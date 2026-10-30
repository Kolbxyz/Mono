package com.wander.mono.ui.theme

import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.rectangle
import com.wander.mono.data.settings.IconShape

/**
 * The shape full-bleed app icons are cut to; null for [IconShape.System], which keeps the
 * system's own mask. Static: it changes only when the user picks another shape.
 */
val LocalIconMask = staticCompositionLocalOf<Shape?> { null }

/** [IconShape] as a Compose shape, from Material's expressive shapes; null keeps the system's mask. */
@Composable
internal fun IconShape.toMask(): Shape? = when (this) {
    IconShape.System -> null
    IconShape.Circle -> MaterialShapes.Circle.toShape()
    IconShape.Squircle -> Squircle.toShape()
    IconShape.Cookie -> MaterialShapes.Cookie9Sided.toShape()
    IconShape.Clover -> MaterialShapes.Clover4Leaf.toShape()
}

/** Material has no squircle: a square whose fully smoothed corners take most of each side. */
private val Squircle: RoundedPolygon =
    RoundedPolygon.rectangle(rounding = CornerRounding(radius = SQUIRCLE_RADIUS, smoothing = 1f)).normalized()

/** Of the default rectangle's half-side of 1. */
private const val SQUIRCLE_RADIUS = 0.6f
