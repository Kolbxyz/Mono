package com.wander.mono.ui.drawer

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos

/**
 * Geometry of the letter rail. The rail splits its height into one equal slot per letter, a letter
 * drawn at the centre of its slot; a finger at [y] is over the slot it falls in.
 */
internal object RailMath {
    /** How many letters on each side of the finger the wave reaches before it is flat again. */
    const val WAVE_REACH = 4f

    /**
     * The letter under a finger at [y] on a rail [height] tall; clamped, so sliding past an end
     * keeps the end letter.
     */
    fun letterAt(y: Float, height: Float, count: Int): Int {
        if (count <= 0 || height <= 0f) return -1
        return (y / height * count).toInt().coerceIn(0, count - 1)
    }

    /** The finger's position in letter units: 0f is the centre of the first letter, 1f of the second. */
    fun fingerSlot(y: Float, height: Float, count: Int): Float =
        if (count <= 0 || height <= 0f) 0f else y / height * count - HALF

    /**
     * How far a letter [distance] slots from the finger bows out, from 1 under the finger to 0 at
     * [WAVE_REACH] slots and beyond: a raised cosine, so the wave has no corner at its peak or feet.
     */
    fun waveFraction(distance: Float): Float {
        val d = abs(distance)
        return if (d >= WAVE_REACH) 0f else ((cos(d / WAVE_REACH * PI) + 1.0) / 2.0).toFloat()
    }

    /**
     * Every how many letters one is drawn so each drawn letter gets at least [minSlot]: 1 on a
     * phone in portrait, more on a short rail (landscape), where the skipped ones still scrub.
     */
    fun labelStride(height: Float, count: Int, minSlot: Float): Int {
        if (count <= 0 || height <= 0f) return 1
        return ceil(minSlot * count / height).toInt().coerceAtLeast(1)
    }

    private const val HALF = 0.5f
}
