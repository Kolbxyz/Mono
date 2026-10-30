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
 * Tap, swipes, hold, and hold-then-drag on one favourite. A tap launches; a swipe right before
 * the hold shows its notifications, a swipe left runs the action the user chose for it; the hold
 * opens its options at once, under the finger; moving on from the hold closes them again and lifts
 * the row into a drag (in edit mode the hold alone lifts it). A drag of the list before the hold
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
    val editing = callbacks.value.editing
    val swipes = Swipes(
        right = !editing && callbacks.value.onSwipeRight != null,
        left = !editing && callbacks.value.onSwipeLeft != null,
    )
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
        BeforeHold.SwipedLeft -> {
            interactions.tryEmit(PressInteraction.Cancel(press))
            callbacks.value.onSwipeLeft?.invoke()
            return@awaitEachGesture
        }
        BeforeHold.Cancelled -> {
            interactions.tryEmit(PressInteraction.Cancel(press))
            return@awaitEachGesture
        }
    }
    callbacks.value.onHeld()
    // Outside edit mode the options open now; the options' window leaves this touch to home.
    val options = !callbacks.value.editing
    if (options) callbacks.value.onHold()
    val movedBy = if (options) awaitMoveAfterHold(down.id) else 0f
    if (movedBy == null) {
        interactions.tryEmit(PressInteraction.Release(press))
        return@awaitEachGesture
    }
    if (options) callbacks.value.onHoldCancelled()
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
 * slop (a drag), or null when it lifts first (the options stay open).
 */
private suspend fun AwaitPointerEventScope.awaitMoveAfterHold(id: PointerId): Float? {
    var total = Offset.Zero
    while (total.getDistance() <= viewConfiguration.touchSlop) {
        val change = awaitPointerEvent().changes.firstOrNull { it.id == id }
        change?.consume()
        if (change == null || !change.pressed) return null
        total += change.positionChange()
    }
    return total.y
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

    data object SwipedLeft : BeforeHold

    /** Taken by someone else (the list scrolling) or gone out of the row. */
    data object Cancelled : BeforeHold
}

/** Which horizontal swipes a favourite answers to right now. */
private class Swipes(val right: Boolean, val left: Boolean)

/**
 * `waitForUpOrCancellation`, also ending as a swipe once the finger has moved past the touch slop
 * sideways more than vertically, in a direction [swipes] allows. A vertical drag is the list's: it
 * consumes it.
 */
private suspend fun AwaitPointerEventScope.awaitTapOrSwipe(id: PointerId, swipes: Swipes): BeforeHold {
    var moved = Offset.Zero
    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.firstOrNull { it.id == id }
        val ended = when {
            change == null -> BeforeHold.Cancelled
            event.changes.all { it.changedToUp() } -> BeforeHold.Tap(change)
            event.changes.any { it.isConsumed || it.isOutOfBounds(size, extendedTouchPadding) } -> BeforeHold.Cancelled
            else -> {
                moved += change.positionChange()
                swipeOf(moved, swipes)?.also { change.consume() } ?: scrolledAway()
            }
        }
        if (ended != null) return ended
    }
}

/** A swipe once [moved] passes the touch slop sideways more than vertically, in a direction [swipes] allows. */
private fun AwaitPointerEventScope.swipeOf(moved: Offset, swipes: Swipes): BeforeHold? {
    val sideways = abs(moved.x) > viewConfiguration.touchSlop && abs(moved.x) > abs(moved.y)
    return when {
        !sideways -> null
        moved.x > 0 -> BeforeHold.SwipedRight.takeIf { swipes.right }
        else -> BeforeHold.SwipedLeft.takeIf { swipes.left }
    }
}

/**
 * Cancelled once the list has taken the drag: it consumes its scroll after this row's pass, so the
 * final pass sees it.
 */
private suspend fun AwaitPointerEventScope.scrolledAway(): BeforeHold? =
    BeforeHold.Cancelled.takeIf { awaitPointerEvent(PointerEventPass.Final).changes.any { it.isConsumed } }
