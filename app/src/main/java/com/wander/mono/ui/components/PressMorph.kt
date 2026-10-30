package com.wander.mono.ui.components

import androidx.compose.material3.toPath
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.lerp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon

/**
 * How a pressed element changes shape under the finger, on top of shrinking ([expressivePress]).
 * Pick [Corners] for anything that is not square (rows, cards, docks), [Polygons] for square
 * badges and icons, where Material's expressive shapes can melt into one another.
 */
@Immutable
sealed interface PressMorph {
    /** A rounded rectangle whose corners go from [resting] to [pressed]; holds at any aspect ratio. */
    data class Corners(val resting: Dp, val pressed: Dp) : PressMorph

    /**
     * One polygon melting into another, e.g. `MaterialShapes.Cookie9Sided` into `Circle`. The
     * polygons are stretched to the box, so keep this to square elements.
     */
    data class Polygons(val resting: RoundedPolygon, val pressed: RoundedPolygon) : PressMorph {
        /** Matched once per instance, and only when drawn: matching features is the costly part. */
        internal val morph by lazy(LazyThreadSafetyMode.NONE) { Morph(resting, pressed) }
    }

    companion object {
        /**
         * The default press for a row or card: corners of [resting] (the theme's `shapes.large`
         * is 26 dp) tighten to a slightly squarer [PRESSED_CORNER_FRACTION] of it.
         */
        fun squarer(resting: Dp): PressMorph = Corners(resting, resting * PRESSED_CORNER_FRACTION)
    }
}

/** How much of its corner radius a [PressMorph.squarer] shape keeps when fully pressed. */
const val PRESSED_CORNER_FRACTION = 0.6f

/**
 * This morph as a shape at [progress] (0 resting, 1 pressed). [progress] is read when the outline
 * is built, so a node that observes that read (`background`, `border`) redraws without
 * recomposing its owner. A spring's overshoot is clamped: shapes do not extrapolate well.
 */
internal fun PressMorph.shapeAt(progress: () -> Float): Shape = when (this) {
    is PressMorph.Corners -> CornerMorphShape(this, progress)
    is PressMorph.Polygons -> PolygonMorphShape(morph, progress)
}

private class CornerMorphShape(private val corners: PressMorph.Corners, private val progress: () -> Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val radius = with(density) { lerp(corners.resting, corners.pressed, progress().coerceIn(0f, 1f)).toPx() }
        return Outline.Rounded(RoundRect(size.toRect(), CornerRadius(radius.coerceAtMost(size.minDimension / 2))))
    }
}

/**
 * The path is fitted to the box from its own measured bounds rather than an assumed
 * normalisation, so it can never drift off-centre.
 */
private class PolygonMorphShape(private val morph: Morph, private val progress: () -> Float) : Shape {
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
