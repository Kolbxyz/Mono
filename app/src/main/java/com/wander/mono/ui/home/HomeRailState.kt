package com.wander.mono.ui.home

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.wander.mono.ui.drawer.RailLanding
import com.wander.mono.ui.drawer.RailScrub

/**
 * Home's own letter rail, Niagara's: a finger on home's edge ([scrub]) brings it out, and while the
 * finger holds a letter home shows that letter's apps in place of its own ([shown]). Lifting on a
 * letter [land]s it for the drawer to open at ([landing]); lifting on ★, the top, leaves home as it is.
 * Once landed, the letter stays shown until the drawer covers home ([drawerCovers]), so nothing
 * flickers as one hands over to the other.
 */
@Stable
class HomeRailState {
    val scrub = RailScrub()
    val landing = RailLanding()

    /** The rail entry under the finger: 0 is ★, then the letters; -1 while no finger is down. */
    var held by mutableIntStateOf(-1)
        private set

    private var landed by mutableIntStateOf(-1)

    /** The letter home shows instead of itself, held or just landed; -1 for home itself. */
    val shown: Int get() = if (held > 0) held else landed

    /**
     * The rail heard [entry] under the finger (-1 once it lifts) on a rail of [letters] letters;
     * a lift on a letter lands it and runs [onLand].
     */
    fun hold(entry: Int, letters: Int, onLand: () -> Unit) {
        if (entry >= 0) {
            held = entry
            return
        }
        val last = held
        held = -1
        if (last in 1..letters) {
            landed = last
            landing.land(last)
            onLand()
        }
    }

    /** The drawer now covers home: home goes back to showing itself, out of sight. */
    fun drawerCovers() {
        landed = -1
    }
}
