package com.wander.mono.ui.components

import android.os.Build
import androidx.activity.BackEventCompat
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

/**
 * A full-screen surface giving way to what is under it: as [morph] goes 0 to 1 it shrinks into a
 * rounded card with a shadowed edge and leans toward the side a back swipe pulls it ([lean], from
 * [backLean]). It stays opaque: only [fade] (its own presence) makes it see-through. A back swipe
 * and a page coming or going drive the same morph, so the swipe seeks the page's exit and the page
 * opens out of the card it leaves as.
 */
fun Modifier.backMorph(morph: () -> Float, lean: () -> Float, fade: () -> Float = { 1f }): Modifier = graphicsLayer {
    val amount = morph().coerceIn(0f, 1f)
    val scale = lerp(1f, MORPH_SCALE, amount)
    scaleX = scale
    scaleY = scale
    translationX = lean() * amount * size.width * MORPH_SHIFT
    alpha = fade().coerceIn(0f, 1f)
    if (amount > 0f) {
        shape = RoundedCornerShape(MORPH_CORNER * amount)
        clip = true
        // The card's edge, so it stands apart from the out-of-focus layer under it.
        shadowElevation = MORPH_ELEVATION.toPx() * amount
    } else {
        shape = RectangleShape
        clip = false
        shadowElevation = 0f
    }
}

/**
 * Which way a back swipe from [swipeEdge] pulls a surface: toward the middle, so away from the
 * left edge is right (1), away from the right edge is left (-1); a swipe from no edge does not lean.
 */
fun backLean(swipeEdge: Int): Float = when (swipeEdge) {
    BackEventCompat.EDGE_LEFT -> 1f
    BackEventCompat.EDGE_RIGHT -> -1f
    else -> 0f
}

/**
 * The layer under a [backMorph] surface, as deep behind it as [depth] (0 to 1): it steps back,
 * dims under [depthScrim] of [scrim] and, with [blur], goes out of focus ([outOfFocus]). A back
 * swipe brings it forward as the surface gives way. The dim is drawn outside the scale, edge to edge.
 */
fun Modifier.depthBlur(depth: () -> Float, scrim: Color, blur: Boolean): Modifier {
    val dim = depthScrim(scrim, blur)
    return this
        .drawWithContent {
            drawContent()
            val amount = depth().coerceIn(0f, 1f)
            if (amount > 0f) drawRect(dim, alpha = amount)
        }
        .graphicsLayer {
            val scale = lerp(1f, DEPTH_SCALE, depth().coerceIn(0f, 1f))
            scaleX = scale
            scaleY = scale
        }
        .outOfFocus(depth, blur)
}

/**
 * A layer blurred out of focus as far as [depth] (0 to 1), with [blur] and on Android 12 and up
 * (the first with render effects); no blur otherwise. It neither dims nor scales.
 */
fun Modifier.outOfFocus(depth: () -> Float, blur: Boolean): Modifier = graphicsLayer {
    renderEffect = blurEffect(DEPTH_BLUR.toPx() * depth().coerceIn(0f, 1f), blur)
}

/**
 * The dim over a layer behind a surface: a light one where the layer also blurs, the blur already
 * setting it apart; [scrim] in full where it cannot (motion reduced, or before Android 12).
 */
fun depthScrim(scrim: Color, blur: Boolean): Color = if (blurs(blur)) scrim.copy(alpha = BLURRED_DIM) else scrim

/** Whether a [blur] asked for can be drawn: render effects came with Android 12. */
private fun blurs(blur: Boolean): Boolean = blur && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/** A blur under a pixel is invisible; dropping it then spares the GPU the pass. */
private fun blurEffect(radius: Float, blur: Boolean): RenderEffect? =
    if (blurs(blur) && radius >= 1f) BlurEffect(radius, radius) else null

/** How small a surface gets at the far end of its morph. */
private const val MORPH_SCALE = 0.85f

/** How far a surface leans, as a share of its width, at the far end of its morph. */
private const val MORPH_SHIFT = 0.06f

/** A surface's corner radius at the far end of its morph. */
private val MORPH_CORNER = 32.dp

/** How high a surface's card lifts off the layer under it at the far end of its morph. */
private val MORPH_ELEVATION = 8.dp

/** How small the layer under a surface gets when fully behind it. */
private const val DEPTH_SCALE = 0.94f

/** How out of focus the layer under a surface gets when fully behind it. */
private val DEPTH_BLUR = 32.dp

/** The dim over a blurred layer, as alpha: light, as the blur does most of the work. */
private const val BLURRED_DIM = 0.14f
