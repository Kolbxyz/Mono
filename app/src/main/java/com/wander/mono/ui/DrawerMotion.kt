package com.wander.mono.ui

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.components.FollowDrag
import com.wander.mono.ui.components.backResistance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * How far the drawer is open, 0 to 1, following the finger while dragged and springing to open or
 * closed when released (by fling velocity, or past [THRESHOLD] of its travel). Whether it is open
 * belongs to [LauncherViewModel]: a release that changes it calls [onSettle], and [onSettled] once
 * the drawer has come to rest there; the view model's own changes (tap, launch, home press, back)
 * come in through [follow].
 */
@Stable
class DrawerMotion internal constructor(
    open: Boolean,
    private val scope: CoroutineScope,
    internal var openSpec: FiniteAnimationSpec<Float>,
    internal var closeSpec: FiniteAnimationSpec<Float>,
) : FollowDrag {
    /** Overshoots 0..1 briefly while springing; draw with [shown]. */
    var progress by mutableFloatStateOf(if (open) 1f else 0f)
        private set

    /** [progress] clamped, for drawing. */
    val shown: Float get() = progress.coerceIn(0f, 1f)

    /** Pixels of finger travel from closed to open; set from the window's height. */
    var travel = 1f
        set(value) {
            field = value.coerceAtLeast(1f)
        }

    internal var flingVelocity = 0f
    internal var onSettle: (Boolean) -> Unit = {}
    internal var onArm: () -> Unit = {}
    internal var onSettled: () -> Unit = {}

    private var target = open
    private var dragging = false
    private var animation: Job? = null

    override val isDragging: Boolean get() = dragging

    override fun dragBy(deltaY: Float) {
        if (!dragging) {
            animation?.cancel()
            dragging = true
        }
        val wasArmed = armed(progress)
        progress = (progress - deltaY / travel).coerceIn(0f, 1f)
        if (!wasArmed && armed(progress)) onArm()
    }

    override fun release(velocityY: Float) {
        if (!dragging) return
        dragging = false
        val openVelocity = -velocityY
        val open = when {
            openVelocity > flingVelocity -> true
            openVelocity < -flingVelocity -> false
            else -> if (armed(progress)) !target else target
        }
        val changed = open != target
        if (changed) {
            target = open
            onSettle(open)
        }
        // Springing back to where the drag started is no new state to mark.
        animateTo(open, openVelocity / travel, onEnd = if (changed) onSettled else null)
    }

    /** The view model opened or closed the drawer; a release that already did so is left to finish. */
    fun follow(open: Boolean, animated: Boolean) {
        if (open == target && !dragging) return
        dragging = false
        target = open
        if (animated) {
            animateTo(open, 0f)
        } else {
            animation?.cancel()
            progress = if (open) 1f else 0f
        }
    }

    /** Predictive back: the open drawer gives way a little as the back gesture grows. */
    fun back(backProgress: Float) {
        animation?.cancel()
        progress = 1f - backResistance(backProgress) * BACK_TRAVEL
    }

    fun backCancelled() = animateTo(target, 0f)

    /** Whether letting go now would change the state the drag started from. */
    private fun armed(value: Float): Boolean = if (target) value < 1f - THRESHOLD else value > THRESHOLD

    /** [onEnd] runs only if the animation finishes; a drag or another change cancels it first. */
    private fun animateTo(open: Boolean, velocity: Float, onEnd: (() -> Unit)? = null) {
        animation?.cancel()
        val spec = if (open) openSpec else closeSpec
        animation = scope.launch {
            animate(progress, if (open) 1f else 0f, velocity, spec) { value, _ -> progress = value }
            onEnd?.invoke()
        }
    }

    private companion object {
        /** Share of the travel past which a slow release changes state. */
        const val THRESHOLD = 0.3f

        /** How much of the travel a full back gesture takes the drawer before it lets go. */
        const val BACK_TRAVEL = 0.2f
    }
}

/** The drawer's motion, starting [open] as the view model last left it; [onSettle] reports a release. */
@Composable
fun rememberDrawerMotion(open: Boolean, onSettle: (Boolean) -> Unit): DrawerMotion {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val motionScheme = MaterialTheme.motionScheme
    val openSpec = motionScheme.defaultSpatialSpec<Float>()
    val closeSpec = motionScheme.fastSpatialSpec<Float>()
    val fling = with(LocalDensity.current) { FLING_VELOCITY.toPx() }
    val motion = remember(scope) { DrawerMotion(open, scope, openSpec, closeSpec) }
    SideEffect {
        motion.openSpec = openSpec
        motion.closeSpec = closeSpec
        motion.flingVelocity = fling
        motion.onSettle = onSettle
        motion.onArm = { haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate) }
        motion.onSettled = { haptics.performHapticFeedback(HapticFeedbackType.GestureEnd) }
    }
    return motion
}

/** Per second: a flick at least this fast opens or closes the drawer whatever its position. */
private val FLING_VELOCITY = 400.dp
