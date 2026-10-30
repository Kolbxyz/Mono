package com.wander.mono.ui.home

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.components.rememberMonoHaptics
import com.wander.mono.ui.theme.LocalReducedMotion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * The hero clock: a gentle pull down past the top of home's list grows the clock toward a display
 * size and brings in a short "today" summary, following the finger; letting go springs it back on
 * the theme's motion. [progress] overshoots below 0 briefly as it settles, which the clock shows as
 * a small bounce; draw the summary with [shown].
 */
@Stable
class HeroPull internal constructor(private val scope: CoroutineScope) {
    var progress by mutableFloatStateOf(0f)
        private set

    /** [progress] clamped, for what fades. */
    val shown: Float get() = progress.coerceIn(0f, 1f)

    internal var enabled = true
    internal var distance = 1f
    internal lateinit var settleSpec: FiniteAnimationSpec<Float>
    internal var onFull: () -> Unit = {}

    private var pulled = 0f
    private var settle: Job? = null

    /** Takes [deltaY] px of a pull past the list's top, down positive; returns how much it took. */
    fun pullBy(deltaY: Float): Float {
        if (!enabled || (deltaY < 0f && pulled <= 0f)) return 0f
        settle?.cancel()
        val before = pulled
        pulled = (pulled + deltaY).coerceAtLeast(0f)
        val wasFull = progress >= 1f
        progress = heroProgress(pulled, distance)
        if (!wasFull && progress >= 1f) onFull()
        return pulled - before
    }

    /** The finger lifted: the clock springs back to its resting size. */
    fun release() {
        if (pulled == 0f && progress == 0f) return
        pulled = 0f
        settle?.cancel()
        settle = scope.launch { animate(progress, 0f, animationSpec = settleSpec) { value, _ -> progress = value } }
    }
}

/**
 * The hero pull. With a swipe-down action ([pullDownActive]) it peaks just short of the 72 dp at
 * which `rememberPullUpConnection` hands off (shade, quick settings, search), so the clock has
 * grown when the shade takes over; without one it has a longer, calmer travel and a tick at full.
 * Off under reduced motion: home then pulls as it always did.
 */
@Composable
fun rememberHeroPull(pullDownActive: Boolean): HeroPull {
    val scope = rememberCoroutineScope()
    val haptics = rememberMonoHaptics()
    val reduced = LocalReducedMotion.current
    val spec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val distance = with(LocalDensity.current) { (if (pullDownActive) HANDOFF_TRAVEL else FREE_TRAVEL).toPx() }
    val hero = remember(scope) { HeroPull(scope) }
    SideEffect {
        hero.enabled = !reduced
        hero.distance = distance
        hero.settleSpec = spec
        hero.onFull = if (pullDownActive) {
            {}
        } else {
            { haptics.threshold() }
        }
    }
    return hero
}

/**
 * Home's swipes ([gestures]) with the hero pull layered under them: [gestures] sees every delta
 * first (the drawer's drag, the swipe-down hand-off), and the hero takes the downward rest past the
 * top of the list, and the way back up while that same pull lasts.
 */
@Composable
fun rememberHeroConnection(gestures: NestedScrollConnection, hero: HeroPull): NestedScrollConnection =
    remember(gestures, hero) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val taken = gestures.onPreScroll(available, source)
                if (taken != Offset.Zero || source != NestedScrollSource.UserInput || available.y >= 0f) return taken
                return Offset(0f, hero.pullBy(available.y))
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val taken = gestures.onPostScroll(consumed, available, source)
                if (source != NestedScrollSource.UserInput || available.y <= 0f) return taken
                // The swipe-down hand-off takes the whole leftover too; the hero follows the same finger.
                return Offset(0f, maxOf(taken.y, hero.pullBy(available.y)))
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                hero.release()
                return gestures.onPreFling(available)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                hero.release()
                return gestures.onPostFling(consumed, available)
            }
        }
    }

/** How far the finger travels to grow the clock fully while a swipe-down action waits at 72 dp. */
private val HANDOFF_TRAVEL = 64.dp

/** The same with no swipe-down action: the hero is all the pull does, so it can take its time. */
private val FREE_TRAVEL = 128.dp
