package com.wander.mono.ui.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon

/**
 * A shape part-way along [morph], at [progress] (read when the outline is built, so a spring
 * redraws the shape without recomposing its owner). The path is fitted to the box from its own
 * measured bounds rather than an assumed normalisation, so it can never drift off-centre.
 */
private class MorphShape(private val morph: Morph, private val progress: () -> Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = morph.toPath(progress().coerceIn(0f, 1f), Path())
        val bounds = path.getBounds()
        // A degenerate bound would put NaNs into the outline; a plain box is the honest answer.
        if (bounds.width <= 0f || bounds.height <= 0f) return Outline.Rectangle(size.toRect())
        val sx = size.width / bounds.width
        val sy = size.height / bounds.height
        path.transform(Matrix().apply { scale(sx, sy) })
        path.translate(Offset(-bounds.left * sx, -bounds.top * sy))
        return Outline.Generic(path)
    }
}

/** A shape that relaxes from [resting] into [pressed] while held, on the theme's fast spatial spring. */
@Composable
fun rememberPressMorphShape(resting: RoundedPolygon, pressed: RoundedPolygon, isPressed: Boolean): Shape {
    val progress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "pressMorph",
    )
    val morph = remember(resting, pressed) { Morph(resting, pressed) }
    return remember(morph) { MorphShape(morph) { progress } }
}
