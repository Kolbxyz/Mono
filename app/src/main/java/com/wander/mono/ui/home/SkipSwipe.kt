package com.wander.mono.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.components.rememberMonoHaptics
import kotlin.math.abs

/**
 * What a released horizontal drag on the now-playing card means. Dragging left brings in the next
 * track, as in a carousel. A fling decides on its own, unless it flicks back against a drag that
 * had passed [distance], which reads as changing one's mind. Directions the session does not
 * support resolve to null, so the card just springs back.
 */
internal fun skipDirectionFor(
    offset: Float,
    velocity: Float,
    distance: Float,
    flingVelocity: Float,
    canNext: Boolean,
    canPrevious: Boolean,
): SkipDirection? {
    val flung = abs(velocity) >= flingVelocity
    val dragged = abs(offset) >= distance
    val push = when {
        flung && dragged && offset * velocity < 0f -> 0f
        flung -> velocity
        dragged -> offset
        else -> 0f
    }
    return when {
        push < 0f -> SkipDirection.Next.takeIf { canNext }
        push > 0f -> SkipDirection.Previous.takeIf { canPrevious }
        else -> null
    }
}

/**
 * Horizontal swipe to skip: the content follows the finger (damped), ticks when the drag passes
 * the skip distance, confirms with a haptic when it skips, and settles back with the motion scheme.
 */
@Composable
internal fun Modifier.skipSwipe(
    canNext: Boolean,
    canPrevious: Boolean,
    onSkip: (SkipDirection) -> Unit,
): Modifier {
    val density = LocalDensity.current
    val distance = with(density) { SKIP_DISTANCE.toPx() }
    val flingVelocity = with(density) { SKIP_FLING_VELOCITY.toPx() }
    val haptics = rememberMonoHaptics()
    val settle = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val currentOnSkip by rememberUpdatedState(onSkip)
    var drag by remember { mutableFloatStateOf(0f) }
    var armed by remember { mutableStateOf(false) }
    // The settle back; a new drag stops it, so the two never write the offset at once.
    val settling = remember { Animatable(0f) }

    val state = rememberDraggableState { delta ->
        drag += delta
        val past = skipDirectionFor(drag, 0f, distance, flingVelocity, canNext, canPrevious) != null
        if (past && !armed) haptics.threshold()
        armed = past
    }
    return draggable(
        state = state,
        orientation = Orientation.Horizontal,
        enabled = canNext || canPrevious,
        onDragStarted = { settling.stop() },
        onDragStopped = { velocity ->
            skipDirectionFor(drag, velocity, distance, flingVelocity, canNext, canPrevious)?.let {
                haptics.confirm()
                currentOnSkip(it)
            }
            armed = false
            settling.snapTo(drag)
            settling.animateTo(0f, settle) { drag = value }
        },
    ).graphicsLayer { translationX = drag * DRAG_FOLLOW }
}

/** How far the card travels per pixel of drag: less than 1, so it feels anchored to its row. */
private const val DRAG_FOLLOW = 0.5f

private val SKIP_DISTANCE = 72.dp

/** Per second. */
private val SKIP_FLING_VELOCITY = 600.dp
