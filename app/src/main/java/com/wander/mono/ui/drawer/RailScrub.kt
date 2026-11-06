package com.wander.mono.ui.drawer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates

/**
 * A scrub of the letter rail begun on home's edge: the finger lands on home's invisible strip
 * ([RailScrubStrip]), home's own rail appears under it, and the same finger drives that rail
 * ([FollowRailScrub]) until it lifts. Positions are window pixels.
 */
@Stable
class RailScrub {
    /** A finger from home's edge is down. */
    var active by mutableStateOf(false)
        private set

    var fingerX by mutableFloatStateOf(0f)
        private set

    var fingerY by mutableFloatStateOf(0f)
        private set

    fun start(x: Float, y: Float) {
        fingerX = x
        fingerY = y
        active = true
    }

    fun move(x: Float, y: Float) {
        fingerX = x
        fingerY = y
    }

    fun end() {
        active = false
    }
}

/** One moment of a [RailScrub] as the rail sees it. */
private data class ScrubFrame(val active: Boolean, val x: Float, val y: Float, val laidOut: Boolean)

/**
 * Lets [scrub] drive [finger]: a scrub already under way when the rail first lays out presses it
 * where the finger is, every move after moves it, and the finger lifting lifts it. [railCoordinates]
 * places the rail in the window; until it and its letters are laid out the finger is left alone, as
 * nothing is on screen yet to scrub.
 */
@Composable
internal fun FollowRailScrub(scrub: RailScrub, finger: RailFinger, railCoordinates: () -> LayoutCoordinates?) {
    LaunchedEffect(scrub, finger) {
        var pressed = false
        snapshotFlow {
            ScrubFrame(scrub.active, scrub.fingerX, scrub.fingerY, laidOut = finger.lettersHeight > 0)
        }.collect { frame ->
            val rail = railCoordinates()?.takeIf { it.isAttached && frame.laidOut }
            val local = rail?.windowToLocal(Offset(frame.x, frame.y))
            when {
                frame.active && local != null && !pressed -> {
                    pressed = true
                    finger.press(local.x, local.y)
                }
                frame.active && local != null -> finger.move(local.x, local.y)
                !frame.active && pressed -> {
                    pressed = false
                    // From home's edge, a finger that never moved is no tap on the lock.
                    finger.lift()
                }
            }
        }
    }
}
