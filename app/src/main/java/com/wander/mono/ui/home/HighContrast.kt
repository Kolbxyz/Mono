package com.wander.mono.ui.home

/**
 * How opaque the home scrim is: none when the user chose a text shadow instead, the legibility
 * [alpha] otherwise; with [highContrast] it is always there, raised by 0.2 (spec 9).
 */
internal fun homeScrimAlpha(alpha: Float, textShadow: Boolean, highContrast: Boolean): Float = when {
    highContrast -> (alpha + HIGH_CONTRAST_BOOST).coerceAtMost(1f)
    textShadow -> 0f
    else -> alpha
}

private const val HIGH_CONTRAST_BOOST = 0.2f
