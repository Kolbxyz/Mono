package com.wander.mono.ui.drawer

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Home's invisible way into the letter rail, Niagara's: a narrow strip down the whole of the rail's
 * edge ([atStart] puts it on the start edge, as the rail), from under the status bar to just above
 * the [bottomClearance] home keeps for its button. A finger landing on it while [enabled] starts
 * [scrub]; home's rail follows the same finger from there ([FollowRailScrub]). The strip keeps the
 * finger whatever shows over it, and is kept out of the system's back gesture where the thumb lands
 * most, as far as Android allows an edge to be.
 */
@Composable
fun BoxScope.RailScrubStrip(scrub: RailScrub, enabled: Boolean, atStart: Boolean, bottomClearance: Dp) {
    val anchor = remember { StripAnchor() }
    val maxExclusion = with(LocalDensity.current) { MAX_EXCLUSION.toPx() }
    // A strip removed mid-scrub (edit mode, a new layout) must not leave the rail thinking a finger is down.
    DisposableEffect(scrub) { onDispose { if (scrub.active) scrub.end() } }
    val catching = enabled || scrub.active
    Box(
        Modifier
            .align(if (atStart) Alignment.CenterStart else Alignment.CenterEnd)
            .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
            // Clear of home's button along the bottom, which keeps its own tap.
            .padding(bottom = bottomClearance)
            .fillMaxHeight()
            .width(STRIP_WIDTH)
            .onGloballyPositioned { anchor.coordinates = it }
            // Off, the strip catches nothing: home's rows under it keep their touches.
            .then(if (catching) Modifier.scrubTouches(scrub, anchor, maxExclusion) else Modifier),
    )
}

/** Starts [scrub] on a finger landing here and feeds it the finger's moves, in window pixels, till it lifts. */
private fun Modifier.scrubTouches(scrub: RailScrub, anchor: StripAnchor, maxExclusion: Float) =
    systemGestureExclusion { ScrubBand.exclusion(it.size.width.toFloat(), it.size.height.toFloat(), maxExclusion) }
        .pointerInput(scrub, anchor) {
            awaitEachGesture {
                val down = awaitFirstDown()
                down.consume()
                val at = anchor.toWindow(down.position)
                scrub.start(at.x, at.y)
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
