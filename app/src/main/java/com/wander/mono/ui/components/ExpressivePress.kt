package com.wander.mono.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp

/**
 * Mono's one press response, the Expressive "it landed" cue: while [interactionSource] is
 * pressed, the element shrinks to [pressedScale] and, given a [morph], its outline changes shape
 * and clips the content to it. Both ride a single progress on the theme's fast spatial spring, so
 * reduced motion (`NoMotionScheme`) makes them instant with no extra code.
 *
 * Adopting it:
 * - Pass the same `MutableInteractionSource` the `clickable` / `Surface` uses, so the press,
 *   ripple and semantics agree. Inside a scrolling list, add [instantPress] so the press shows the
 *   moment the finger lands rather than after the list's tap delay.
 * - Put it before `clickable` and before any `background`, so the content scales and clips
 *   together. Drop a separate `.clip(shape)` that [morph] now draws.
 * - Rows and cards: `PressMorph.squarer(MaterialTheme.shapes.large…)`, or no morph for pills.
 *   Square badges: [rememberPressShape] with [PressMorph.Polygons] for the badge's background.
 *
 * ```kotlin
 * val interaction = remember { MutableInteractionSource() }
 * Row(
 *     Modifier
 *         .expressivePress(interaction, PressMorph.squarer(26.dp))
 *         .instantPress(interaction)
 *         .clickable(interaction, ripple(), onClick = onOpen),
 * ) { … }
 * ```
 */
@Composable
fun Modifier.expressivePress(
    interactionSource: InteractionSource,
    morph: PressMorph? = null,
    pressedScale: Float = ROW_PRESSED_SCALE,
): Modifier {
    val progress = rememberPressProgress(interactionSource)
    return graphicsLayer {
        // Read here, in the layer block, so the spring redraws the layer and never recomposes.
        val p = progress.value
        scaleX = lerp(1f, pressedScale, p)
        scaleY = scaleX
        if (morph != null) {
            // A new outline per frame of the press; the layer only rebuilds it when the shape changes.
            shape = morph.shapeAt { p }
            clip = true
        }
    }
}

/**
 * [morph] as a shape that follows [interactionSource]'s press on the same spring as
 * [expressivePress]: for a part of the pressed element that changes shape on its own, like the
 * settings hub's category badge relaxing into a circle while its row shrinks. Use it with
 * `background` or `border`, which redraw as the spring moves without recomposing.
 */
@Composable
fun rememberPressShape(interactionSource: InteractionSource, morph: PressMorph): Shape {
    val progress = rememberPressProgress(interactionSource)
    return remember(morph, progress) { morph.shapeAt { progress.value } }
}

/** 0 at rest, 1 held, on the theme's fast spatial spring (which may overshoot slightly). */
@Composable
private fun rememberPressProgress(interactionSource: InteractionSource): State<Float> {
    val pressed by interactionSource.collectIsPressedAsState()
    return animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "expressivePress",
    )
}

/** How far a pressed row shrinks; small, as rows are wide. */
const val ROW_PRESSED_SCALE = 0.96f
