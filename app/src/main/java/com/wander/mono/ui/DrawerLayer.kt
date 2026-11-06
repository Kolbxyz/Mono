package com.wander.mono.ui

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.util.lerp
import com.wander.mono.ui.components.rememberPullDownConnection
import com.wander.mono.ui.drawer.LocalDrawerLift
import kotlin.coroutines.cancellation.CancellationException

/**
 * The drawer over home, rising with [motion] and fading in as it comes: composed while it is
 * [open] or any part of it shows. Dragging down past the top of its list, or on anything that does
 * not scroll, pulls it back down with the finger; predictive back lets it give way before closing
 * through [onBack]. [onClosed] runs once it has closed and nothing of it shows, so whatever it was
 * showing stays put while it fades out. How far it is still drawn below its resting place reaches the
 * content as [LocalDrawerLift], so its backdrop can stay put while only the content rises; opened in
 * place ([DrawerMotion.openInPlace]) it does not rise at all.
 */
@Composable
fun DrawerLayer(
    motion: DrawerMotion,
    open: Boolean,
    backEnabled: Boolean,
    onBack: () -> Unit,
    onClosed: () -> Unit,
    content: @Composable () -> Unit,
) {
    val showing by remember(motion) { derivedStateOf { motion.progress > 0f } }
    val closed = !open && !showing
    val currentOnClosed by rememberUpdatedState(onClosed)
    LaunchedEffect(closed) { if (closed) currentOnClosed() }
    if (closed) return
    val pullDown = rememberPullDownConnection(motion)
    // The search field and headers do not scroll; this lets drags on them reach pullDown too.
    val stillDrag = rememberScrollableState { 0f }
    // Opened in place, it only fades: no lift, so what home showed stays where it was.
    val lift = remember(motion) { { if (motion.inPlace) 0f else motion.lift() } }
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
                translationY = lift()
                alpha = motion.shown
            }
            .nestedScroll(pullDown)
            .scrollable(stillDrag, Orientation.Vertical),
    ) {
        CompositionLocalProvider(LocalDrawerLift provides lift, content = content)
    }
}

/**
 * Home behind the drawer fades out as the drawer rises; applied to all of home and to its scrim's
 * layer alike, so the wallpaper is never left bare at its edges.
 */
fun Modifier.fadeBehindDrawer(motion: DrawerMotion): Modifier = graphicsLayer { alpha = 1f - motion.shown }

/**
 * Home's content behind the drawer shrinks back as the drawer rises, as far as under any
 * full-screen overlay ([OverlayLevel.Screen]); its scrim does not.
 */
fun Modifier.shrinkBehindDrawer(motion: DrawerMotion): Modifier = graphicsLayer {
    val scale = lerp(1f, OverlayLevel.Screen.scale, motion.shown)
    scaleX = scale
    scaleY = scale
}

/**
 * Share of the window height the drawer rises through, 1:1 with the finger; [DrawerMotion.travel]
 * must use the same share so a drag maps to exactly that distance.
 */
const val TRAVEL_FRACTION = 1f / 3f
