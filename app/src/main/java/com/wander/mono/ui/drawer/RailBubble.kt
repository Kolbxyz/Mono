package com.wander.mono.ui.drawer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import kotlin.math.roundToInt

/**
 * The letter being scrubbed, shown large beside the finger, clear of the wave. It grows from a
 * circle into a nine-sided cookie as the scrub starts, and shrinks back and fades on release.
 */
@Composable
internal fun RailBubble(letter: String, visible: Boolean, fingerY: () -> Float, modifier: Modifier = Modifier) {
    val motion = MaterialTheme.motionScheme
    val fade by animateFloatAsState(if (visible) 1f else 0f, motion.defaultEffectsSpec())
    val progress by animateFloatAsState(if (visible) 1f else 0f, motion.defaultSpatialSpec())
    if (fade == 0f) return

    val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided) }
    val path = remember { Path() }
    val matrix = remember { Matrix() }
    val color = MaterialTheme.colorScheme.primaryContainer
    Box(
        modifier
            .offset {
                val size = BUBBLE_SIZE.toPx()
                IntOffset(
                    x = -(BUBBLE_GAP.toPx() + size).roundToInt(),
                    y = (fingerY() - size / 2).roundToInt(),
                )
            }
            .size(BUBBLE_SIZE)
            .graphicsLayer { alpha = fade }
            .drawBehind {
                morph.toPath(progress.coerceIn(0f, 1f), path)
                matrix.reset()
                matrix.scale(size.width, size.height)
                path.transform(matrix)
                path.translate(size.center - path.getBounds().center)
                drawPath(path, color)
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            letter,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

private val BUBBLE_SIZE = 72.dp

/** Space between the bubble and the rail, so the fully bowed letter never slips under the bubble. */
private val BUBBLE_GAP = WAVE_AMPLITUDE + 8.dp
