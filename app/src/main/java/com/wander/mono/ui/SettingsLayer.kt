package com.wander.mono.ui

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import com.wander.mono.ui.components.backResistance
import kotlin.coroutines.cancellation.CancellationException

/**
 * Settings over home (and over the drawer when opened from it, so back returns there), sliding in
 * from a quarter of the screen to the side as [presence] comes in; what is under it recedes by
 * the same presence ([recede]). Predictive back on the hub shrinks and fades it toward its edge as
 * the swipe grows, the layers under it coming forward with it, then closes it through [onBack]; a
 * page's own back, registered inside [content] after this one, comes first. [animateExit] is false
 * when it was closed while Mono was not on screen: nothing to animate.
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
    PredictiveBackHandler(enabled = open) { events ->
        try {
            events.collect { presence.swipeBack(it.progress) }
            onBack()
        } catch (_: CancellationException) {
            // The swipe was abandoned: settings grows back, on the presence's own scope.
            presence.backCancelled()
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                val shown = presence.shown
                val given = backResistance(presence.back)
                val scale = lerp(1f, BACK_MIN_SCALE, given)
                translationX = (1f - shown) * size.width / SLIDE_FRACTION
                scaleX = scale
                scaleY = scale
                alpha = shown * lerp(1f, BACK_MIN_ALPHA, given)
                transformOrigin = TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.5f)
            },
    ) {
        content()
    }
}

private const val SLIDE_FRACTION = 4

/** How small settings gets at the far end of a back swipe, before it commits. */
private const val BACK_MIN_SCALE = 0.9f

/** How faint settings gets at the far end of a back swipe, before it commits. */
private const val BACK_MIN_ALPHA = 0.6f
