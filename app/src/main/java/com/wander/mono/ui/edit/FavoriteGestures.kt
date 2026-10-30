package com.wander.mono.ui.edit

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.input.pointer.positionChange
import kotlin.math.abs

/**
 * Tap, swipe right, hold, and hold-then-drag on one favourite. A tap launches; a swipe right
 * before the hold shows its notifications; holding then letting go opens its options; holding then
 * moving (or just holding, in edit mode) lifts it and drags it. A drag of the list before the hold
 * timeout is a scroll and is left to the list.
 */
internal suspend fun PointerInputScope.detectFavoriteGestures(
    interactions: MutableInteractionSource,
    callbacks: State<FavoriteGestureCallbacks>,
) = awaitEachGesture {
    val down = awaitFirstDown()
    down.consume()
    val press = PressInteraction.Press(down.position)
    interactions.tryEmit(press)
    val swipes = !callbacks.value.editing && callbacks.value.onSwipeRight != null
    val beforeHold = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) { awaitTapOrSwipe(down.id, swipes) }
    when (beforeHold) {
        null -> Unit
        is BeforeHold.Tap -> {
            beforeHold.up.consume()
            interactions.tryEmit(PressInteraction.Release(press))
            if (!callbacks.value.editing) callbacks.value.onTap()
            return@awaitEachGesture
        }
        BeforeHold.SwipedRight -> {
            interactions.tryEmit(PressInteraction.Cancel(press))
            callbacks.value.onSwipeRight?.invoke()
            return@awaitEachGesture
        }
        BeforeHold.Cancelled -> {
            interactions.tryEmit(PressInteraction.Cancel(press))
            return@awaitEachGesture
        }
    }
    callbacks.value.onHeld()
    val movedBy = if (callbacks.value.editing) 0f else awaitMoveAfterHold(down.id, callbacks)
    if (movedBy == null) {
        interactions.tryEmit(PressInteraction.Release(press))
        return@awaitEachGesture
    }
    interactions.tryEmit(PressInteraction.Cancel(press))
    callbacks.value.onLift()
    callbacks.value.onDrag(movedBy)
    try {
        dragUntilUp(down.id, callbacks)
    } finally {
        callbacks.value.onDrop()
    }
}

/**
 * The drag handle in edit mode lifts its row on touch, without the hold. Its down is consumed,
 * so the row's own gesture detector leaves it alone.
 */
internal suspend fun PointerInputScope.detectHandleDrag(callbacks: State<FavoriteGestureCallbacks>) =
    awaitEachGesture {
        val down = awaitFirstDown()
        down.consume()
        callbacks.value.onHeld()
        callbacks.value.onLift()
        try {
            dragUntilUp(down.id, callbacks)
        } finally {
            callbacks.value.onDrop()
        }
    }

/**
 * After a hold outside edit mode: the vertical distance moved once the finger passes the touch
 * slop (a drag), or null when it lifts first (the options).
 */
private suspend fun AwaitPointerEventScope.awaitMoveAfterHold(
    id: PointerId,
    callbacks: State<FavoriteGestureCallbacks>,
): Float? {
    var total = Offset.Zero
    var released = false
    while (!released && total.getDistance() <= viewConfiguration.touchSlop) {
        val change = awaitPointerEvent().changes.firstOrNull { it.id == id }
        change?.consume()
        when {
            change == null -> released = true
            !change.pressed -> {
                released = true
                callbacks.value.onHold()
            }
            else -> total += change.positionChange()
        }
    }
    return if (released) null else total.y
}

private suspend fun AwaitPointerEventScope.dragUntilUp(id: PointerId, callbacks: State<FavoriteGestureCallbacks>) {
    while (true) {
        val change = awaitPointerEvent().changes.firstOrNull { it.id == id } ?: return
        if (!change.pressed) {
            change.consume()
            return
        }
        callbacks.value.onDrag(change.positionChange().y)
        change.consume()
    }
}

/** How a press ended before the hold timeout. */
private sealed interface BeforeHold {
    class Tap(val up: PointerInputChange) : BeforeHold

    data object SwipedRight : BeforeHold

    /** Taken by someone else (the list scrolling) or gone out of the row. */
    data object Cancelled : BeforeHold
}

/**
 * `waitForUpOrCancellation`, also ending as a swipe once the finger has moved past the touch slop
 * to the right more than vertically, when [swipes]. A vertical drag is the list's: it consumes it.
 */
private suspend fun AwaitPointerEventScope.awaitTapOrSwipe(id: PointerId, swipes: Boolean): BeforeHold {
    var moved = Offset.Zero
    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.firstOrNull { it.id == id } ?: return BeforeHold.Cancelled
        if (event.changes.all { it.changedToUp() }) return BeforeHold.Tap(change)
        if (event.changes.any { it.isConsumed || it.isOutOfBounds(size, extendedTouchPadding) }) {
            return BeforeHold.Cancelled
        }
        moved += change.positionChange()
        if (swipes && moved.x > viewConfiguration.touchSlop && moved.x > abs(moved.y)) {
            change.consume()
            return BeforeHold.SwipedRight
        }
        // The list consumes its scroll after this row's pass; the final pass sees it.
        if (awaitPointerEvent(PointerEventPass.Final).changes.any { it.isConsumed }) return BeforeHold.Cancelled
    }
}
