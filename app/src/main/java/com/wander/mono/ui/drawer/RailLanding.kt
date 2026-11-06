package com.wander.mono.ui.drawer

import androidx.compose.runtime.Stable

/**
 * The letter a scrub on home's edge let go on, for the drawer it opens to start at. Home's rail
 * [land]s it as the finger lifts; the drawer [take]s it once, as it first composes, so a later
 * opening (a swipe, a tap) starts at the top as usual. Not snapshot state: read once, never observed.
 */
@Stable
class RailLanding {
    private var entry = NONE

    /** The finger lifted on rail entry [railEntry] (1 is the first letter). */
    fun land(railEntry: Int) {
        entry = railEntry
    }

    /** The entry landed on since the last take, or [NONE]; clears it either way. */
    fun take(): Int = entry.also { entry = NONE }

    companion object {
        const val NONE = -1
    }
}
