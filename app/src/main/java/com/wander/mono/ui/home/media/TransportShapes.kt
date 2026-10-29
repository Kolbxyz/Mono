package com.wander.mono.ui.home.media

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * Play/pause's shape says the state, as in Wanda: a pill while paused, a rounded square while
 * playing, tighter under the finger. The corner is read at draw time, so the spring never recomposes.
 */
@Composable
internal fun rememberPlayPauseShape(isPlaying: Boolean, isPressed: Boolean): Shape {
    val target = when {
        isPlaying && isPressed -> PLAYING_PRESSED
        isPlaying -> PLAYING
        isPressed -> PAUSED_PRESSED
        else -> PAUSED
    }
    val corner by animateFloatAsState(target, MaterialTheme.motionScheme.fastSpatialSpec(), label = "play-corner")
    return remember { SidedCornerShape(start = { corner }, end = { corner }) }
}

/**
 * A skip button at one end of the connected transport: its outer side fully round, its inner side
 * (towards play/pause) tight, rounding out under the finger, as Expressive connected groups do.
 */
@Composable
internal fun rememberConnectedShape(leading: Boolean, isPressed: Boolean): Shape {
    val inner by animateFloatAsState(
        if (isPressed) PAUSED else CONNECTED_INNER,
        MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "connected-corner",
    )
    return remember(leading) {
        if (leading) {
            SidedCornerShape(start = { PAUSED }, end = { inner })
        } else {
            SidedCornerShape(start = { inner }, end = { PAUSED })
        }
    }
}

/**
 * Rounded corners as fractions of the short side, one for the start side and one for the end, in
 * the layout direction.
 */
private class SidedCornerShape(private val start: () -> Float, private val end: () -> Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val startRadius = CornerRadius(size.minDimension * start().coerceIn(0f, MAX_FRACTION))
        val endRadius = CornerRadius(size.minDimension * end().coerceIn(0f, MAX_FRACTION))
        val ltr = layoutDirection == LayoutDirection.Ltr
        val left = if (ltr) startRadius else endRadius
        val right = if (ltr) endRadius else startRadius
        return Outline.Rounded(
            RoundRect(
                rect = size.toRect(),
                topLeft = left,
                topRight = right,
                bottomRight = right,
                bottomLeft = left,
            ),
        )
    }
}

private const val MAX_FRACTION = 0.5f

/** Half the short side: a circle, or a pill on a wide button. */
private const val PAUSED = 0.5f
private const val PAUSED_PRESSED = 0.4f
private const val PLAYING = 0.3f
private const val PLAYING_PRESSED = 0.22f

/** The inner corners of the connected skip buttons. */
private const val CONNECTED_INNER = 0.18f
