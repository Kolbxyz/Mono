package com.wander.mono.ui.components

import android.os.Build
import androidx.activity.BackEventCompat
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

/**
 * A full-screen surface giving way to what is under it: as [morph] goes 0 to 1 it shrinks into a
 * rounded card, leans toward the side a back swipe pulls it ([lean], from [backLean]) and fades a
 * little, on top of [fade] (its own presence). A back swipe and a page coming or going drive the
 * same morph, so the swipe seeks the page's exit and the page opens out of the card it leaves as.
 */
fun Modifier.backMorph(morph: () -> Float, lean: () -> Float, fade: () -> Float = { 1f }): Modifier = graphicsLayer {
    val amount = morph().coerceIn(0f, 1f)
    val scale = lerp(1f, MORPH_SCALE, amount)
    scaleX = scale
    scaleY = scale
    translationX = lean() * amount * size.width * MORPH_SHIFT
    alpha = fade().coerceIn(0f, 1f) * lerp(1f, MORPH_ALPHA, amount)
    if (amount > 0f) {
        shape = RoundedCornerShape(MORPH_CORNER * amount)
        clip = true
    } else {
        shape = RectangleShape
        clip = false
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
 * dims under [scrim] and, with [blur] and on Android 12 and up (the first with render effects;
 * below it there is only the dim), goes out of focus. A back swipe brings it forward as the
 * surface gives way. The scrim is drawn outside the scale, edge to edge.
 */
fun Modifier.depthBlur(depth: () -> Float, scrim: Color, blur: Boolean): Modifier = this
    .drawWithContent {
        drawContent()
        val amount = depth().coerceIn(0f, 1f)
        if (amount > 0f) drawRect(scrim, alpha = amount)
    }
    .graphicsLayer {
        val amount = depth().coerceIn(0f, 1f)
        val scale = lerp(1f, DEPTH_SCALE, amount)
        scaleX = scale
        scaleY = scale
        val radius = DEPTH_BLUR.toPx() * amount
        // A blur under a pixel is invisible; dropping it then spares the GPU the pass.
        renderEffect = if (blur && radius >= 1f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            BlurEffect(radius, radius)
        } else {
            null
        }
    }

/** How small a surface gets at the far end of its morph. */
private const val MORPH_SCALE = 0.88f

/** How faint a surface gets at the far end of its morph, before its own fade. */
private const val MORPH_ALPHA = 0.9f

/** How far a surface leans, as a share of its width, at the far end of its morph. */
private const val MORPH_SHIFT = 0.06f

/** A surface's corner radius at the far end of its morph. */
private val MORPH_CORNER = 32.dp

/** How small the layer under a surface gets when fully behind it. */
private const val DEPTH_SCALE = 0.94f

/** How out of focus the layer under a surface gets when fully behind it. */
private val DEPTH_BLUR = 24.dp
