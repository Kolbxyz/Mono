package com.wander.mono.ui.components

import kotlin.math.abs
import kotlin.math.sign

/**
 * Eases a 0..1 predictive-back progress so it resists as it grows: a quadratic ease-out, fast
 * off the edge and slowing toward the end.
 */
fun backResistance(progress: Float): Float {
    val p = progress.coerceIn(0f, 1f)
    return 1f - (1f - p) * (1f - p)
}

/**
 * Maps a raw finger displacement to a damped one that approaches, but never reaches, [limit]: it
 * tracks the finger closely at first and stiffens the further it is pulled. Sign is preserved.
 */
fun rubberBand(displacement: Float, limit: Float): Float {
    if (limit <= 0f) return 0f
    val magnitude = abs(displacement)
    return limit * (1f - 1f / (magnitude / limit + 1f)) * sign(displacement)
}
