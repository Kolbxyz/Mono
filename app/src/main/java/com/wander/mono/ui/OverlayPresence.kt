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
import com.wander.mono.ui.components.backResistance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * How much of a full-screen overlay (settings) is in: [presence] 0 to 1 as it enters and leaves,
 * and [back], how far a predictive back swipe has taken it. Both drive the overlay and, through
 * [cover], the layers under it, so a back swipe seeks one transition: the overlay gives way and
 * what is under it comes forward together. A committed swipe leaves [back] where it was, so the
 * overlay leaves from where the finger took it.
 */
@Stable
class OverlayPresence internal constructor(open: Boolean, private val scope: CoroutineScope) {
    /** Overshoots 0..1 briefly while springing; draw with [shown]. */
    var presence by mutableFloatStateOf(if (open) 1f else 0f)
        private set

    var back by mutableFloatStateOf(0f)
        private set

    internal lateinit var enterSpec: FiniteAnimationSpec<Float>
    internal lateinit var exitSpec: FiniteAnimationSpec<Float>

    private var target = open
    private var animation: Job? = null
    private var backSettle: Job? = null

    val shown: Float get() = presence.coerceIn(0f, 1f)

    /** How much the overlay covers the layers under it: they recede by this much. */
    val cover: Float get() = coverOf(shown, back)

    /** The view model opened or closed it; [animated] false when Mono was off screen. */
    fun follow(open: Boolean, animated: Boolean) {
        if (open == target) return
        target = open
        animation?.cancel()
        // Each opening starts whole, whatever the last back swipe left.
        if (open) {
            backSettle?.cancel()
            back = 0f
        }
        if (!animated) {
            presence = if (open) 1f else 0f
            return
        }
        animation = scope.launch {
            animate(presence, if (open) 1f else 0f, animationSpec = if (open) enterSpec else exitSpec) { value, _ ->
                presence = value
            }
        }
    }

    fun swipeBack(progress: Float) {
        backSettle?.cancel()
        back = progress
    }

    /** The swipe was abandoned: the overlay grows back. */
    fun backCancelled() {
        backSettle?.cancel()
        backSettle = scope.launch { animate(back, 0f, animationSpec = exitSpec) { value, _ -> back = value } }
    }
}

/** The overlay's presence on the theme's motion: in on the default spatial spec, out on the fast one. */
@Composable
fun rememberOverlayPresence(open: Boolean): OverlayPresence {
    val scope = rememberCoroutineScope()
    val motion = MaterialTheme.motionScheme
    val enter = motion.defaultSpatialSpec<Float>()
    val exit = motion.fastSpatialSpec<Float>()
    val presence = remember(scope) { OverlayPresence(open, scope) }
    SideEffect {
        presence.enterSpec = enter
        presence.exitSpec = exit
    }
    return presence
}

/**
 * How much an overlay [shown] that far, taken [back] by a back swipe, covers what is under it:
 * the swipe hands back up to [BACK_GIVE] of it before it commits.
 */
internal fun coverOf(shown: Float, back: Float): Float =
    shown.coerceIn(0f, 1f) * (1f - backResistance(back) * BACK_GIVE)

/** Share of an overlay's cover a full back swipe hands back to the layers under it. */
internal const val BACK_GIVE = 0.5f
