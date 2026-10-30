package com.wander.mono.ui.edit

/** What a favourite row does with its gestures; read fresh on every event. */
internal class FavoriteGestureCallbacks(
    val editing: Boolean,
    val onTap: () -> Unit,
    /** A swipe right before the hold, outside edit mode; null when the row has nothing to show for it. */
    val onSwipeRight: (() -> Unit)?,
    /** A swipe left before the hold, outside edit mode; null when the user chose no action for it. */
    val onSwipeLeft: (() -> Unit)?,
    /** The hold timeout passed: the moment for the pick-up haptic. */
    val onHeld: () -> Unit,
    /** Held and let go without moving, outside edit mode: the row's options. */
    val onHold: () -> Unit,
    /** Held: the row lifts, entering edit mode first when needed. */
    val onLift: () -> Unit,
    val onDrag: (Float) -> Unit,
    val onDrop: () -> Unit,
)
