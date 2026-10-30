package com.wander.mono.ui.drawer

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Home's invisible way into the letter rail, Niagara's: a narrow strip down the rail's edge ([atStart]
 * puts it on the start edge, as the rail), over the lower part of home where the thumb rests on the
 * favourites. A finger landing on it while [enabled] runs [onStart] (open the drawer, on its list)
 * and starts [scrub]; the drawer's rail follows the same finger from there ([FollowRailScrub]), and
 * lifting it leaves the drawer at that letter. The strip keeps the finger however the drawer covers
 * it, and is kept out of the system's back gesture where the thumb lands most, as far as Android
 * allows an edge to be.
 */
@Composable
fun BoxScope.RailScrubStrip(scrub: RailScrub, enabled: Boolean, atStart: Boolean, onStart: () -> Unit) {
    val currentOnStart by rememberUpdatedState(onStart)
    val anchor = remember { StripAnchor() }
    val maxExclusion = with(LocalDensity.current) { MAX_EXCLUSION.toPx() }
    // A strip removed mid-scrub (edit mode, a new layout) must not leave the rail thinking a finger is down.
    DisposableEffect(scrub) { onDispose { if (scrub.active) scrub.end() } }
    val catching = enabled || scrub.active
    Box(
        Modifier
            .align(if (atStart) Alignment.BottomStart else Alignment.BottomEnd)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
            // Clear of home's search dock along the bottom, which keeps its own tap.
            .padding(bottom = DOCK_CLEARANCE)
            .fillMaxHeight(BAND_FRACTION)
            .width(STRIP_WIDTH)
            .onGloballyPositioned { anchor.coordinates = it }
            // Off, the strip catches nothing: home's rows under it keep their touches.
            .then(if (catching) Modifier.scrubTouches(scrub, anchor, maxExclusion) { currentOnStart() } else Modifier),
    )
}

/** Starts [scrub] on a finger landing here and feeds it the finger's moves, in window pixels, till it lifts. */
private fun Modifier.scrubTouches(scrub: RailScrub, anchor: StripAnchor, maxExclusion: Float, onStart: () -> Unit) =
    systemGestureExclusion { ScrubBand.exclusion(it.size.width.toFloat(), it.size.height.toFloat(), maxExclusion) }
        .pointerInput(scrub, anchor) {
            awaitEachGesture {
                val down = awaitFirstDown()
                down.consume()
                val at = anchor.toWindow(down.position)
                scrub.start(at.x, at.y)
                onStart()
                try {
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id && it.pressed }
                        change?.let {
                            it.consume()
                            val moved = anchor.toWindow(it.position)
                            scrub.move(moved.x, moved.y)
                        }
                    } while (change != null)
                } finally {
                    scrub.end()
                }
            }
        }

/** Where the strip sits in the window, for turning its touches into window positions. */
private class StripAnchor {
    var coordinates: LayoutCoordinates? = null

    fun toWindow(local: Offset): Offset = coordinates?.takeIf { it.isAttached }?.localToWindow(local) ?: local
}

/** The strip's share of the window's height, from the bottom: where the favourites sit. */
private const val BAND_FRACTION = 0.6f

/** Home's search dock and the space around it, at the bottom of the band. */
private val DOCK_CLEARANCE = 72.dp

/** Wide enough for a thumb on the edge, narrow enough to leave the rows' labels alone. */
private val STRIP_WIDTH = 32.dp

/** Android honours at most this much of an edge excluded from the back gesture. */
private val MAX_EXCLUSION = 200.dp

/** The strip's geometry, apart from Compose so it can be checked on its own. */
internal object ScrubBand {
    /**
     * The part of a strip [width] by [height] px kept from the back gesture: all of it when it fits
     * in [max] px, else [max] px centred on the strip, where the thumb most often lands.
     */
    fun exclusion(width: Float, height: Float, max: Float): Rect {
        val tall = height.coerceAtMost(max).coerceAtLeast(0f)
        val top = (height - tall) / 2
        return Rect(0f, top, width, top + tall)
    }
}
