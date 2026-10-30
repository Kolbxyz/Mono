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
 * A scrub of the letter rail begun on home's edge, before the drawer and its rail exist: the finger
 * lands on home's invisible strip ([RailScrubStrip]), the drawer rises, and the same finger goes on
 * driving the drawer's [LetterRail] until it lifts, leaving the drawer at that letter. Positions are
 * window pixels. [drawerLift] is how far below its resting place the rising drawer is drawn right
 * now, so the rail maps the finger against where its letters come to rest, not where they pass.
 */
@Stable
class RailScrub(internal val drawerLift: () -> Float) {
    /** A finger from home's edge is down. */
    var active by mutableStateOf(false)
        private set

    var fingerX by mutableFloatStateOf(0f)
        private set

    var fingerY by mutableFloatStateOf(0f)
        private set

    /**
     * This opening of the drawer came from a scrub: it opens on the list, never on the keyboard,
     * even after the finger lifts. Cleared once the drawer has closed ([drawerClosed]).
     */
    var openedDrawer by mutableStateOf(false)
        private set

    fun start(x: Float, y: Float) {
        fingerX = x
        fingerY = y
        openedDrawer = true
        active = true
    }

    fun move(x: Float, y: Float) {
        fingerX = x
        fingerY = y
    }

    fun end() {
        active = false
    }

    fun drawerClosed() {
        openedDrawer = false
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
            val local = railCoordinates()?.takeIf { it.isAttached && frame.laidOut }?.let {
                restingLocal(it, Offset(frame.x, frame.y), scrub.drawerLift())
            }
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

/**
 * The window point [window] in [rail]'s own pixels as they will be once the drawer has risen: the
 * rail is drawn [lift] px below that now, so the point is that much further down its letters.
 */
private fun restingLocal(rail: LayoutCoordinates, window: Offset, lift: Float): Offset {
    val drawn = rail.windowToLocal(window)
    return Offset(drawn.x, drawn.y + lift)
}
