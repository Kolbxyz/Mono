package com.wander.mono.ui.home

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Draws the legibility scrim only behind the header and the list (spec 5.4): [color] at full
 * strength behind each, fading out over [fade] px into the empty space between them, so the
 * wallpaper shows untouched there. Positions are read at draw time, so scrolling only redraws.
 */
fun Modifier.homeScrim(color: Color, headerBottom: () -> Float, listTop: () -> Float, fade: Float): Modifier =
    if (color.alpha == 0f) {
        this
    } else {
        drawBehind {
            val stops = scrimStops(size.height, headerBottom(), listTop(), fade)
                .map { (position, strength) -> position to color.copy(alpha = color.alpha * strength) }
            drawRect(Brush.verticalGradient(*stops.toTypedArray()))
        }
    }

/**
 * Gradient stops as (fraction of [height], strength 0..1): full behind the header (down to
 * [headerBottom]) and the list (from [listTop]), clear in between once the gap leaves room for
 * both [fade]s; otherwise full everywhere, as the content then fills the screen anyway.
 */
internal fun scrimStops(height: Float, headerBottom: Float, listTop: Float, fade: Float): List<Pair<Float, Float>> {
    if (height <= 0f || listTop - headerBottom < 2 * fade) return listOf(0f to 1f, 1f to 1f)
    fun at(y: Float) = y / height
    return listOf(
        0f to 1f,
        at(headerBottom) to 1f,
        at(headerBottom + fade) to 0f,
        at(listTop - fade) to 0f,
        at(listTop) to 1f,
        1f to 1f,
    )
}
