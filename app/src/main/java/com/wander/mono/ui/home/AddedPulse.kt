package com.wander.mono.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.graphicsLayer
import com.wander.mono.core.apps.AppKey
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * The favourite just added, if exactly one was: [after] is [before] with one key more. A reorder,
 * a removal or a restored backup adding several is not an addition to point out.
 */
internal fun addedFavorite(before: List<AppKey>, after: List<AppKey>): AppKey? {
    if (after.size != before.size + 1) return null
    val known = before.toSet()
    return after.singleOrNull { it !in known }
}

/**
 * A newly added favourite pulses once (spec 7.2): it swells and settles on the fast spatial spec
 * while a tonal highlight with home's card corners comes and goes behind it. [onDone] runs
 * after, so it plays only once.
 */
@Composable
internal fun Modifier.addedPulse(active: Boolean, onDone: () -> Unit): Modifier {
    val motion = MaterialTheme.motionScheme
    val highlightColor = MaterialTheme.colorScheme.secondaryContainer
    val scale = remember { Animatable(1f) }
    val highlight = remember { Animatable(0f) }
    val done by rememberUpdatedState(onDone)
    val spatial = motion.fastSpatialSpec<Float>()
    val effects = motion.defaultEffectsSpec<Float>()
    LaunchedEffect(active) {
        if (!active) {
            // Cut short (the drawer came back over it): the row rests as it was.
            scale.snapTo(1f)
            highlight.snapTo(0f)
            return@LaunchedEffect
        }
        coroutineScope {
            launch {
                highlight.animateTo(1f, effects)
                highlight.animateTo(0f, effects)
            }
            scale.animateTo(PULSE_SCALE, spatial)
            scale.animateTo(1f, spatial)
        }
        done()
    }
    return this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .drawBehind {
            if (highlight.value > 0f) {
                drawRoundRect(
                    highlightColor.copy(alpha = highlight.value * HIGHLIGHT_ALPHA),
                    cornerRadius = CornerRadius(HomeCardCorner.toPx()),
                )
            }
        }
}

private const val PULSE_SCALE = 1.04f
private const val HIGHLIGHT_ALPHA = 0.7f
