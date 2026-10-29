package com.wander.mono.ui.home

import com.wander.mono.core.system.WallpaperTone
import kotlin.math.pow

/**
 * How opaque the surface scrim over the wallpaper must be for [text] to read on it (spec 5.4).
 * Starts from the spec's base: 0.35 for a light theme over a wallpaper that suits dark text,
 * 0.45 otherwise, 0.65 when the wallpaper is busy or unknown. Then raises it until [text] reaches
 * 4.5:1 against the [scrim] blended over each of the wallpaper's main colours. Colours are ARGB.
 */
internal fun scrimAlpha(tone: WallpaperTone?, scrim: Int, text: Int): Float {
    if (tone == null || tone.colors.isEmpty()) return BUSY_ALPHA
    val lightTheme = relativeLuminance(scrim) > HALF
    val base = when {
        isBusy(tone.colors) -> BUSY_ALPHA
        lightTheme && tone.supportsDarkText -> MATCHING_ALPHA
        else -> MISMATCHED_ALPHA
    }
    var alpha = base
    while (alpha < 1f && tone.colors.any { contrast(text, blend(scrim, it, alpha)) < TARGET_CONTRAST }) {
        alpha = (alpha + ALPHA_STEP).coerceAtMost(1f)
    }
    return alpha
}

/** A wallpaper is busy when its main colours are far apart in brightness: no single text colour suits it all. */
internal fun isBusy(colors: List<Int>): Boolean {
    val luminances = colors.map(::relativeLuminance)
    return (luminances.max() - luminances.min()) >= BUSY_SPREAD
}

/** WCAG contrast ratio of two opaque colours, 1 to 21. */
internal fun contrast(first: Int, second: Int): Float {
    val a = relativeLuminance(first)
    val b = relativeLuminance(second)
    return ((maxOf(a, b) + FLARE) / (minOf(a, b) + FLARE)).toFloat()
}

/** [over] at [alpha] composited on opaque [under], per channel in sRGB as the screen does it. */
internal fun blend(over: Int, under: Int, alpha: Float): Int {
    fun channel(shift: Int): Int {
        val top = over shr shift and BYTE
        val bottom = under shr shift and BYTE
        return (top * alpha + bottom * (1 - alpha) + ROUNDING).toInt() and BYTE
    }
    return OPAQUE or (channel(RED) shl RED) or (channel(GREEN) shl GREEN) or channel(BLUE)
}

/** WCAG relative luminance of an ARGB colour, ignoring its alpha. */
internal fun relativeLuminance(color: Int): Double {
    fun linear(shift: Int): Double {
        val c = (color shr shift and BYTE) / BYTE.toDouble()
        return if (c <= SRGB_KNEE) c / SRGB_LINEAR_SLOPE else ((c + SRGB_OFFSET) / (1 + SRGB_OFFSET)).pow(SRGB_GAMMA)
    }
    return LUMA_R * linear(RED) + LUMA_G * linear(GREEN) + LUMA_B * linear(BLUE)
}

private const val MATCHING_ALPHA = 0.35f
private const val MISMATCHED_ALPHA = 0.45f
private const val BUSY_ALPHA = 0.65f
private const val ALPHA_STEP = 0.05f
private const val TARGET_CONTRAST = 4.5f
private const val BUSY_SPREAD = 0.4
private const val HALF = 0.5

private const val FLARE = 0.05
private const val BYTE = 0xFF
private const val OPAQUE = 0xFF shl 24
private const val RED = 16
private const val GREEN = 8
private const val BLUE = 0
private const val ROUNDING = 0.5f
private const val SRGB_KNEE = 0.04045
private const val SRGB_LINEAR_SLOPE = 12.92
private const val SRGB_OFFSET = 0.055
private const val SRGB_GAMMA = 2.4
private const val LUMA_R = 0.2126
private const val LUMA_G = 0.7152
private const val LUMA_B = 0.0722
