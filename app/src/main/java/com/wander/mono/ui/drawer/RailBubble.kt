package com.wander.mono.ui.drawer

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.theme.capFontScale
import kotlin.math.roundToInt

/**
 * The letter being scrubbed, shown large beside the finger, just clear of the bowed letters. It sits
 * on an even twelve-sided cookie that grows in as the scrub starts, turns slowly as the finger moves
 * along the rail, and shrinks back and fades on release. It stays on the rail, [railHeight] tall,
 * at either end, and on the list's side of it: left of a rail on the right ([mirror] 1f), right of
 * one on the left (-1f), in screen terms whatever the layout direction. [modifier] places it at
 * the rail's top corner on that side. With a [symbol] (the private space's lock) it shows that instead.
 */
@Composable
internal fun RailBubble(
    letter: String,
    @DrawableRes symbol: Int?,
    visible: Boolean,
    fingerY: () -> Float,
    railHeight: () -> Float,
    bow: () -> Float,
    mirror: Float,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val fade by animateFloatAsState(if (visible) 1f else 0f, motion.defaultEffectsSpec())
    val grow by animateFloatAsState(if (visible) 1f else 0f, motion.defaultSpatialSpec())
    if (fade == 0f) return

    val cookie = MaterialShapes.Cookie12Sided.toShape()
    Box(
        modifier
            .absoluteOffset {
                val size = BUBBLE_SIZE.toPx()
                IntOffset(
                    x = RailMath.bubbleX(mirror, BUBBLE_GAP.toPx(), size, bow()).roundToInt(),
                    y = RailMath.bubbleTop(fingerY(), size, railHeight()).roundToInt(),
                )
            }
            .size(BUBBLE_SIZE)
            .graphicsLayer {
                alpha = fade
                val scale = SMALLEST + (1f - SMALLEST) * grow
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .matchParentSize()
                // Only the cookie turns; the letter stays upright.
                .graphicsLayer { rotationZ = fingerY() / density * DEGREES_PER_DP }
                .background(MaterialTheme.colorScheme.primaryContainer, cookie),
        )
        if (symbol != null) {
            Icon(
                painterResource(symbol),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(SYMBOL_SIZE),
            )
        } else {
            Text(
                letter,
                // Capped, so the letter stays inside the cookie at the largest font sizes.
                style = MaterialTheme.typography.displaySmall.capFontScale(GLYPH_MAX_FONT_SCALE),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

private val BUBBLE_SIZE = 72.dp

private val SYMBOL_SIZE = 32.dp

/** displaySmall grows with the font scale up to this inside the bubble: still clear of its edges. */
private const val GLYPH_MAX_FONT_SCALE = 1.3f

/** Space between the bubble and the bowed letter under the finger. */
private val BUBBLE_GAP = 16.dp

/** The bubble's scale when it starts to grow in, and when it has shrunk away. */
private const val SMALLEST = 0.6f

/** How far the cookie turns as the finger slides along the rail: a quarter turn per 360 dp. */
private const val DEGREES_PER_DP = 0.25f
