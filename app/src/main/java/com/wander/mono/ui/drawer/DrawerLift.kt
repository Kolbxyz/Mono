package com.wander.mono.ui.drawer

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

/**
 * How far below its resting place the rising drawer is drawn right now, in pixels; read while
 * drawing, never in composition. 0 where nothing rises (the wide layout's side panel).
 */
val LocalDrawerLift = staticCompositionLocalOf<() -> Float> { { 0f } }

/**
 * Fills the whole window with [color] even while the drawer's content is still rising into place
 * by [lift] px: only the content rises, the backdrop under it stays put and simply fades in with the
 * drawer, so no tinted band climbs up the screen with a hard edge.
 */
internal fun Modifier.steadyBackdrop(color: Color, lift: () -> Float): Modifier = drawBehind {
    if (color.alpha <= 0f) return@drawBehind
    val rise = lift().coerceAtLeast(0f)
    drawRect(color, topLeft = Offset(0f, -rise), size = Size(size.width, size.height + rise))
}
