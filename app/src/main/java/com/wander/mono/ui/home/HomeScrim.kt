package com.wander.mono.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

/**
 * Draws the legibility scrim behind the header and the list only (spec 5.4): [color] at full
 * strength at the screen's top and bottom edges, easing out to nothing over [reach] px past the
 * header and before the list. The ease is a smoothstep sampled finely, so the falloff has no visible
 * edge. Positions and [strength] (0..1, the fade while the window is not focused) are read at draw
 * time, so scrolling and the fade only redraw.
 *
 * The outermost [taper] px on every side (read at draw time too) ease to nothing: the caller grows
 * it as the window loses focus, so when the system scales Mono's window (the recents gesture) but not
 * the wallpaper behind it, no opaque band shows a hard edge. At 0 the scrim reaches every edge.
 */
fun Modifier.homeScrim(
    color: Color,
    strength: () -> Float,
    headerBottom: () -> Float,
    listTop: () -> Float,
    reach: Float,
    taper: () -> Float,
): Modifier =
    if (color.alpha == 0f) {
        this
    } else {
        drawBehind {
            val alpha = color.alpha * strength()
            if (alpha <= 0f) return@drawBehind
            val stops = scrimStops(size.height, headerBottom(), listTop(), reach)
                .map { (position, share) -> position to color.copy(alpha = alpha * share) }
            val gradient = Brush.verticalGradient(*stops.toTypedArray())
            val edge = taper()
            // Focused, nothing to taper: no offscreen layer for the mask.
            if (edge < 1f) {
                drawRect(gradient)
            } else {
                drawIntoCanvas { canvas ->
                    canvas.saveLayer(Rect(0f, 0f, size.width, size.height), Paint())
                    drawRect(gradient)
                    drawRect(Brush.horizontalGradient(*edgeMask(size.width, edge)), blendMode = BlendMode.DstIn)
                    drawRect(Brush.verticalGradient(*edgeMask(size.height, edge)), blendMode = BlendMode.DstIn)
                    canvas.restore()
                }
            }
        }
    }

/**
 * Whether Mono's activity is the top resumed one. It stops being as soon as another activity takes
 * over (an app opening, the recents gesture), a little before the window loses focus.
 */
internal val LocalTopResumed = compositionLocalOf { true }

/**
 * The scrim's strength: 1 while the window has focus and Mono is the top resumed activity, easing
 * to 0 as soon as either goes. The recents gesture scales the window but not the wallpaper behind
 * it, so the scrim and the text halo step aside, starting at the earlier of the two.
 */
@Composable
internal fun windowFocusStrength(): State<Float> = animateFloatAsState(
    if (LocalWindowInfo.current.isWindowFocused && LocalTopResumed.current) 1f else 0f,
    MaterialTheme.motionScheme.defaultEffectsSpec(),
    label = "scrim-focus",
)

/** [edgeStops] as gradient stops for a [BlendMode.DstIn] mask: only the alpha counts. */
private fun edgeMask(length: Float, taper: Float): Array<Pair<Float, Color>> =
    edgeStops(length, taper).map { (position, keep) -> position to Color.Black.copy(alpha = keep) }.toTypedArray()

/**
 * A mask along a side of [length] px as (fraction, keep 0..1): 0 at both ends, rising on a smoothstep
 * to 1 over [taper] px, 1 in between.
 */
internal fun edgeStops(length: Float, taper: Float): List<Pair<Float, Float>> {
    if (length <= 0f || taper <= 0f) return listOf(0f to 1f, 1f to 1f)
    val edge = (taper / length).coerceAtMost(HALF_LENGTH)
    val rise = (0..EDGE_STEPS).map { step ->
        val t = step / EDGE_STEPS.toFloat()
        edge * t to smoothstep(0f, 1f, t)
    }
    return rise + rise.reversed().map { (position, keep) -> 1f - position to keep }
}

/**
 * Gradient stops as (fraction of [height], strength 0..1). The top holds full strength over the
 * upper half of the header, then eases to 0 by [reach] below [headerBottom]; the bottom rises from 0
 * at [reach] above [listTop] to full at the screen's bottom edge. As the gap between them closes
 * below room for both falloffs, a floor rises with it, reaching full everywhere when the content
 * meets: the scrim never jumps from one layout to the next.
 */
internal fun scrimStops(height: Float, headerBottom: Float, listTop: Float, reach: Float): List<Pair<Float, Float>> {
    if (height <= 0f) return listOf(0f to 1f, 1f to 1f)
    val topHold = headerBottom * TOP_HOLD
    val topEnd = headerBottom + reach
    val bottomStart = listTop - reach
    val floor = if (reach > 0f) (1f - (listTop - headerBottom) / (2 * reach)).coerceIn(0f, 1f) else 0f
    fun strength(y: Float): Float {
        val top = 1f - smoothstep(topHold, topEnd, y)
        val bottom = smoothstep(bottomStart, height, y)
        return maxOf(top, bottom, floor)
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

/** How far the scrim eases out into the empty space past the header and before the list. */
internal val ScrimReach = 120.dp

/**
 * How far in from each window edge the scrim eases from nothing once the window is unfocused, so no
 * opaque band meets the border as the system scales the window.
 */
internal val ScrimTaper = 24.dp

/** How far down the header the top scrim holds full strength before easing out. */
private const val TOP_HOLD = 0.4f

/** The smoothstep polynomial, 3t² − 2t³. */
private const val SMOOTH_A = 3f
private const val SMOOTH_B = 2f

/** Enough stops that the sampled curve reads as smooth on a tall screen. */
private const val STEPS = 32

/** Stops per edge taper; it spans only a few dozen px. */
private const val EDGE_STEPS = 6

/** On a side shorter than two tapers, each taper takes half. */
private const val HALF_LENGTH = 0.5f
