package com.wander.mono.ui.drawer

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.accessibility.AccessibilityManager

/**
 * Feels reaching a rail entry: the light tick for a letter, the long press buzz for ★ and the lock,
 * so a thumb (or a TalkBack user, who hears but does not see the rail) knows it reached an end.
 */
internal fun View.railTick(tick: RailTick) {
    performHapticFeedback(
        when (tick) {
            RailTick.Letter -> LETTER_TICK
            RailTick.Edge -> HapticFeedbackConstants.LONG_PRESS
        },
    )
}

/**
 * A screen reader explores by touch: the rail is then moved with swipes through its semantics, and
 * ticks there too. Read when it moves, never watched.
 */
internal fun View.screenReaderOn(): Boolean =
    context.getSystemService(AccessibilityManager::class.java)?.isTouchExplorationEnabled == true

/** The light "segment" tick Android 14 added for scrubbing through values; a clock tick before it. */
private val LETTER_TICK = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
} else {
    HapticFeedbackConstants.CLOCK_TICK
}
