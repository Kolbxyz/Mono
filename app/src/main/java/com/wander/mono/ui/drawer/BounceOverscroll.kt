package com.wander.mono.ui.drawer

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.components.rubberBand
import kotlin.math.abs
import kotlin.math.sign

/**
 * A list end that gives like rubber and bounces back, instead of the platform's stretch: dragged
 * past its end the list follows the finger ever more stiffly ([rubberBand], up to [limit] px), and
 * springs back on release; flung into its end it overshoots by the speed it still had, and springs
 * back the same way, on the scheme's [spring]. Draw it with [offset] (a translation down the list,
 * read in the draw phase). Only the list's end it reaches is affected: what a parent takes first
 * (the drawer pulled down from its top) never reaches it.
 */
@Stable
internal class BounceOverscroll(private val limit: Float) : OverscrollEffect {
    internal var spring: FiniteAnimationSpec<Float>? = null

    /** How far past its end the finger has pulled, before resistance. */
    private var pulled by mutableFloatStateOf(0f)

    /** How far the list is drawn from its place: [pulled], resisted. */
    val offset: Float get() = rubberBand(pulled, limit)

    override val isInProgress: Boolean get() = pulled != 0f

    override val node: DelegatableNode = object : Modifier.Node() {}

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset,
    ): Offset {
        // Moving back toward its place first takes up the pull, before the list scrolls again.
        val back = if (pulled != 0f && sign(delta.y) == -sign(pulled)) {
            if (abs(delta.y) >= abs(pulled)) -pulled else delta.y
        } else {
            0f
        }
        pulled += back
        val left = Offset(delta.x, delta.y - back)
        val consumed = performScroll(left)
        val beyond = left.y - consumed.y
        // Only a finger pulls past the end; a fling's overshoot comes from its leftover speed.
        val taken = if (source == NestedScrollSource.UserInput && beyond != 0f) beyond else 0f
        pulled += taken
        return Offset(consumed.x, back + consumed.y + taken)
    }

    override suspend fun applyToFling(velocity: Velocity, performFling: suspend (Velocity) -> Velocity) {
        val consumed = performFling(velocity)
        val leftover = velocity.y - consumed.y
        val spec = spring ?: return
        // A pull lets go, and a fling that hit the end carries on past it, both springing home.
        if (pulled != 0f || abs(leftover) > MIN_BOUNCE_SPEED) {
            animate(pulled, 0f, initialVelocity = leftover, animationSpec = spec) { value, _ -> pulled = value }
        }
    }

    private companion object {
        /** Pixels per second below which a fling reaching the end just stops there. */
        const val MIN_BOUNCE_SPEED = 50f
    }
}

/** A [BounceOverscroll] for a list, bouncing back on the scheme's fast spatial spring (its overshoot). */
@Composable
internal fun rememberBounceOverscroll(): BounceOverscroll {
    val limit = with(LocalDensity.current) { BOUNCE_LIMIT.toPx() }
    val spring = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val bounce = remember(limit) { BounceOverscroll(limit) }
    SideEffect { bounce.spring = spring }
    return bounce
}

/** How far a list end can ever be pulled out, however far the finger goes. */
private val BOUNCE_LIMIT = 96.dp
