package com.wander.mono.ui

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import com.wander.mono.ui.components.backResistance
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.launch

/**
 * Settings over home (and over the drawer when opened from it, so back returns there), sliding in
 * from a quarter of the screen to the side. Predictive back on the hub shrinks and fades it toward
 * its edge as the swipe grows, then closes it through [onBack]; a page's own back, registered
 * inside [content] after this one, comes first. [animateExit] is false when it was closed while
 * Mono was not on screen: nothing to animate.
 */
@Composable
fun SettingsLayer(open: Boolean, animateExit: Boolean, onBack: () -> Unit, content: @Composable () -> Unit) {
    val motion = MaterialTheme.motionScheme
    val scope = rememberCoroutineScope()
    val backProgress = remember { Animatable(0f) }
    val settleSpec = motion.fastSpatialSpec<Float>()
    // Each opening starts whole, whatever the last back swipe left.
    LaunchedEffect(open) { if (open) backProgress.snapTo(0f) }
    PredictiveBackHandler(enabled = open) { events ->
        try {
            events.collect { backProgress.snapTo(it.progress) }
            onBack()
        } catch (_: CancellationException) {
            // The swipe was abandoned: settings grows back, outside this cancelled handler.
            scope.launch { backProgress.animateTo(0f, settleSpec) }
        }
    }
    AnimatedVisibility(
        visible = open,
        enter = slideInHorizontally(motion.defaultSpatialSpec()) { it / SLIDE_FRACTION } +
            fadeIn(motion.defaultEffectsSpec()),
        exit = if (animateExit) {
            slideOutHorizontally(motion.fastSpatialSpec()) { it / SLIDE_FRACTION } + fadeOut(motion.fastEffectsSpec())
        } else {
            ExitTransition.None
        },
    ) {
        Box(
            Modifier.graphicsLayer {
                val given = backResistance(backProgress.value)
                val scale = lerp(1f, BACK_MIN_SCALE, given)
                scaleX = scale
                scaleY = scale
                alpha = lerp(1f, BACK_MIN_ALPHA, given)
                transformOrigin = TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.5f)
            },
        ) {
            content()
        }
    }
}

private const val SLIDE_FRACTION = 4

/** How small settings gets at the far end of a back swipe, before it commits. */
private const val BACK_MIN_SCALE = 0.9f

/** How faint settings gets at the far end of a back swipe, before it commits. */
private const val BACK_MIN_ALPHA = 0.6f
