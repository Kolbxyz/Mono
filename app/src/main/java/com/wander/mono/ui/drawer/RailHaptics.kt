package com.wander.mono.ui.drawer

import android.view.View
import android.view.accessibility.AccessibilityManager
import com.wander.mono.ui.components.MonoHaptics

/**
 * Feels reaching a rail entry: the light scrubbing tick for a letter, the heavier long-press buzz
 * for ★ and the lock, so a thumb (or a TalkBack user, who hears but does not see the rail) knows it
 * reached an end.
 */
internal fun MonoHaptics.railTick(reached: RailTick) = when (reached) {
    RailTick.Letter -> tick()
    RailTick.Edge -> longPress()
}

/**
 * A screen reader explores by touch: the rail is then moved with swipes through its semantics, and
 * ticks there too. Read when it moves, never watched.
 */
internal fun View.screenReaderOn(): Boolean =
    context.getSystemService(AccessibilityManager::class.java)?.isTouchExplorationEnabled == true
