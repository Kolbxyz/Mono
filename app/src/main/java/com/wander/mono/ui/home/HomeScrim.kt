package com.wander.mono.ui.home

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Draws the legibility scrim behind the header and the list only (spec 5.4): [color] at full
 * strength at the screen's top and bottom edges, easing out to nothing over [reach] px past the
 * header and before the list. The ease is a smoothstep sampled finely, so the falloff has no visible
 * edge. Positions are read at draw time, so scrolling only redraws.
 */
fun Modifier.homeScrim(color: Color, headerBottom: () -> Float, listTop: () -> Float, reach: Float): Modifier =
    if (color.alpha == 0f) {
        this
    } else {
        drawBehind {
            val stops = scrimStops(size.height, headerBottom(), listTop(), reach)
                .map { (position, strength) -> position to color.copy(alpha = color.alpha * strength) }
            drawRect(Brush.verticalGradient(*stops.toTypedArray()))
        }
    }

/**
 * Gradient stops as (fraction of [height], strength 0..1). The top holds full strength over the
 * upper half of the header, then eases to 0 by [reach] below [headerBottom]; the bottom rises from 0
 * at [reach] above [listTop] to full at the screen's bottom edge. Full everywhere when the gap
 * between them is too short for both.
 */
internal fun scrimStops(height: Float, headerBottom: Float, listTop: Float, reach: Float): List<Pair<Float, Float>> {
    // No room for both falloffs: the content fills the screen, so it all needs the scrim.
    if (height <= 0f || listTop - headerBottom < 2 * reach) return listOf(0f to 1f, 1f to 1f)
    val topHold = headerBottom * TOP_HOLD
    val topEnd = headerBottom + reach
    val bottomStart = listTop - reach
    fun strength(y: Float): Float {
        val top = 1f - smoothstep(topHold, topEnd, y)
        val bottom = smoothstep(bottomStart, height, y)
        return maxOf(top, bottom)
    }
    return (0..STEPS).map { step ->
        val fraction = step / STEPS.toFloat()
        fraction to strength(fraction * height)
    }
}

/** 0 below [from], 1 above [to], an S-curve in between: no corner at either end. */
private fun smoothstep(from: Float, to: Float, x: Float): Float {
    if (to <= from) return if (x < from) 0f else 1f
    val t = ((x - from) / (to - from)).coerceIn(0f, 1f)
    return t * t * (SMOOTH_A - SMOOTH_B * t)
}

/** How far down the header the top scrim holds full strength before easing out. */
private const val TOP_HOLD = 0.4f

/** The smoothstep polynomial, 3t² − 2t³. */
private const val SMOOTH_A = 3f
private const val SMOOTH_B = 2f

/** Enough stops that the sampled curve reads as smooth on a tall screen. */
private const val STEPS = 32
