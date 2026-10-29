package com.wander.mono.ui.drawer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * The letter being scrubbed, shown large beside the finger, just clear of the bowed letters. It sits
 * on an even twelve-sided cookie that grows in as the scrub starts, turns slowly as the finger moves
 * along the rail, and shrinks back and fades on release.
 */
@Composable
internal fun RailBubble(
    letter: String,
    visible: Boolean,
    fingerY: () -> Float,
    bow: () -> Float,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val fade by animateFloatAsState(if (visible) 1f else 0f, motion.defaultEffectsSpec())
    val grow by animateFloatAsState(if (visible) 1f else 0f, motion.defaultSpatialSpec())
    if (fade == 0f) return

    val cookie = MaterialShapes.Cookie12Sided.toShape()
    Box(
        modifier
            .offset {
                val size = BUBBLE_SIZE.toPx()
                IntOffset(
                    x = -(BUBBLE_GAP.toPx() + size + bow()).roundToInt(),
                    y = (fingerY() - size / 2).roundToInt(),
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
        Text(
            letter,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

private val BUBBLE_SIZE = 72.dp

/** Space between the bubble and the bowed letter under the finger. */
private val BUBBLE_GAP = 16.dp

/** The bubble's scale when it starts to grow in, and when it has shrunk away. */
private const val SMALLEST = 0.6f

/** How far the cookie turns as the finger slides along the rail: a quarter turn per 360 dp. */
private const val DEGREES_PER_DP = 0.25f
