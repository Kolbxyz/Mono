package com.wander.mono.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.Density
import kotlin.math.roundToInt

/**
 * Lays out a list shorter than its room [fraction] of the way down the free space: 0 against the
 * top, 1 at the bottom, rows touching. A longer list scrolls and never uses it. A spring that
 * overshoots is held to the room.
 */
internal data class ListPlacement(val fraction: Float) : Arrangement.Vertical {
    override fun Density.arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) {
        var top = ((totalSize - sizes.sum()).coerceAtLeast(0) * fraction.coerceIn(0f, 1f)).roundToInt()
        sizes.forEachIndexed { index, size ->
            outPositions[index] = top
            top += size
        }
    }
}
