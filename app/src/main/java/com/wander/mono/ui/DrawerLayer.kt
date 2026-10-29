package com.wander.mono.ui

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.util.lerp
import com.wander.mono.ui.components.rememberPullDownConnection
import kotlin.coroutines.cancellation.CancellationException

/**
 * The drawer over home, rising with [motion] and fading in as it comes: composed while it is
 * [open] or any part of it shows. Dragging down past the top of its list, or on anything that does
 * not scroll, pulls it back down with the finger; predictive back lets it give way before closing
 * through [onBack].
 */
@Composable
fun DrawerLayer(
    motion: DrawerMotion,
    open: Boolean,
    backEnabled: Boolean,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    val showing by remember(motion) { derivedStateOf { motion.progress > 0f } }
    if (!open && !showing) return
    val pullDown = rememberPullDownConnection(motion)
    // The search field and headers do not scroll; this lets drags on them reach pullDown too.
    val stillDrag = rememberScrollableState { 0f }
    PredictiveBackHandler(enabled = open && backEnabled) { events ->
        try {
            events.collect { motion.back(it.progress) }
            onBack()
        } catch (_: CancellationException) {
            motion.backCancelled()
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                val shown = motion.shown
                translationY = (1f - shown) * size.height * TRAVEL_FRACTION
                alpha = shown
            }
            .nestedScroll(pullDown)
            .scrollable(stillDrag, Orientation.Vertical),
    ) {
        content()
    }
}

/** Home behind the drawer: it fades and shrinks back a little as the drawer rises. */
fun Modifier.behindDrawer(motion: DrawerMotion): Modifier = graphicsLayer {
    val shown = motion.shown
    alpha = 1f - shown
    val scale = lerp(1f, HOME_SCALE, shown)
    scaleX = scale
    scaleY = scale
}

/**
 * Share of the window height the drawer rises through, 1:1 with the finger; [DrawerMotion.travel]
 * must use the same share so a drag maps to exactly that distance.
 */
const val TRAVEL_FRACTION = 1f / 3f

private const val HOME_SCALE = 0.92f
