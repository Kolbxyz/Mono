package com.wander.mono.ui

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.wander.mono.ui.components.backLean
import com.wander.mono.ui.components.backMorph
import com.wander.mono.ui.components.backResistance
import kotlin.coroutines.cancellation.CancellationException

/**
 * Settings over home (and over the drawer when opened from it, so back returns there), sliding in
 * from a quarter of the screen to the side as [presence] comes in; what is under it recedes by
 * the same presence ([recede]). Predictive back on the hub shrinks it into a rounded card leaning
 * off the swiped edge ([backMorph]) as the swipe grows, the layers under it coming forward with
 * it, then closes it through [onBack]; a page's own back, registered inside [content] after this
 * one, comes first. [animateExit] is false when it was closed while Mono was not on screen:
 * nothing to animate.
 */
@Composable
fun SettingsLayer(
    presence: OverlayPresence,
    open: Boolean,
    animateExit: Boolean,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    LaunchedEffect(open, animateExit) { presence.follow(open, animated = open || animateExit) }
    val currentOpen by rememberUpdatedState(open)
    val composed by remember(presence) { derivedStateOf { currentOpen || presence.presence > 0f } }
    if (!composed) return
    // Which way the last back swipe leans settings; only read while it morphs.
    var lean by remember { mutableFloatStateOf(0f) }
    PredictiveBackHandler(enabled = open) { events ->
        try {
            events.collect {
                lean = backLean(it.swipeEdge)
                presence.swipeBack(it.progress)
            }
            onBack()
        } catch (_: CancellationException) {
            // The swipe was abandoned: settings grows back, on the presence's own scope.
            presence.backCancelled()
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { translationX = (1f - presence.shown) * size.width / SLIDE_FRACTION }
            // Only settings morphs: what is under it is its own layer, receding by the cover.
            .backMorph({ backResistance(presence.back) }, { lean }, fade = { presence.shown }),
    ) {
        content()
    }
}

private const val SLIDE_FRACTION = 4
