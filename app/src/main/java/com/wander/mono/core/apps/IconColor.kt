package com.wander.mono.core.apps

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/*
 * An app's accent colour, read from its icon: the hue that covers the most of the icon, weighted by
 * how vivid each pixel is, so a small saturated logo on a white plate still wins over the plate.
 * A hue histogram over a tiny downscale is all it takes; no palette library. Called off the main
 * thread (the icon cache loads on IO) and cached with the icon.
 */

/** This icon's accent ([dominantAccent]), drawn at [ACCENT_SAMPLE_PX] square. */
fun Drawable.accentColour(): Int? = toBitmap(ACCENT_SAMPLE_PX, ACCENT_SAMPLE_PX).accentColour()

/** This bitmap's accent ([dominantAccent]), from a [ACCENT_SAMPLE_PX] square downscale. */
fun Bitmap.accentColour(): Int? {
    // A hardware bitmap's pixels cannot be read back; such an icon is copied out first.
    val readable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && config == Bitmap.Config.HARDWARE) {
        copy(Bitmap.Config.ARGB_8888, false)
    } else {
        this
    }
    val sample = readable.scale(ACCENT_SAMPLE_PX, ACCENT_SAMPLE_PX)
    val pixels = IntArray(ACCENT_SAMPLE_PX * ACCENT_SAMPLE_PX)
    sample.getPixels(pixels, 0, ACCENT_SAMPLE_PX, 0, 0, ACCENT_SAMPLE_PX, ACCENT_SAMPLE_PX)
    return dominantAccent(pixels)
}

/**
 * The dominant vivid colour of [pixels] (ARGB), opaque; null when too little of the visible icon
 * is coloured to call it the app's colour (a grey or black-and-white icon). Transparent, grey and
 * near-black pixels do not vote. The winning hue bin is chosen with half its neighbours' weight,
 * so a hue on a bin edge is not split in two, and its colour is the vividness-weighted mean of
 * the pixels in it.
 */
fun dominantAccent(pixels: IntArray): Int? {
    val visible = pixels.filter { it ushr ALPHA_SHIFT >= MIN_ALPHA }
    val votes = HueVotes().apply { visible.forEach(::add) }
    return votes.winner(minWeight = MIN_SHARE * visible.size)
}

/** Vividness-weighted votes per hue bin, with each bin's colour sum to average at the end. */
private class HueVotes {
    private val weight = FloatArray(HUE_BINS)
    private val red = FloatArray(HUE_BINS)
    private val green = FloatArray(HUE_BINS)
    private val blue = FloatArray(HUE_BINS)

    fun add(pixel: Int) {
        val r = pixel shr RED_SHIFT and CHANNEL
        val g = pixel shr GREEN_SHIFT and CHANNEL
        val b = pixel and CHANNEL
        val high = max(r, max(g, b))
        val delta = high - min(r, min(g, b))
        val chroma = delta / CHANNEL.toFloat()
        if (chroma < MIN_CHROMA || high < MIN_BRIGHTNESS) return
        val bin = (hue(r, g, b, high, delta) / FULL_TURN * HUE_BINS).toInt().coerceIn(0, HUE_BINS - 1)
        weight[bin] += chroma
        red[bin] += r * chroma
        green[bin] += g * chroma
        blue[bin] += b * chroma
    }

    /** The winning bin's mean colour, or null when its weight is under [minWeight] (or zero). */
    fun winner(minWeight: Float): Int? {
        val best = (0 until HUE_BINS).maxBy { bin ->
            weight[bin] + NEIGHBOUR_SHARE * (weight[(bin + HUE_BINS - 1) % HUE_BINS] + weight[(bin + 1) % HUE_BINS])
        }
        val total = weight[best]
        if (total <= 0f || total < minWeight) return null
        return argb(red[best] / total, green[best] / total, blue[best] / total)
    }
}

/** Hue in degrees, 0 to 360, of a colour whose largest channel is [high] and spread is [delta] (> 0). */
private fun hue(r: Int, g: Int, b: Int, high: Int, delta: Int): Float {
    val sector = when (high) {
        r -> ((g - b).toFloat() / delta).mod(HUE_SECTORS)
        g -> (b - r).toFloat() / delta + GREEN_SECTOR
        else -> (r - g).toFloat() / delta + BLUE_SECTOR
    }
    return sector * FULL_TURN / HUE_SECTORS
}

/** WCAG relative luminance of an ARGB colour, 0 (black) to 1 (white); alpha is ignored. */
fun relativeLuminance(colour: Int): Double {
    fun linear(channel: Int): Double {
        val c = channel / CHANNEL.toDouble()
        return if (c <= SRGB_KNEE) c / SRGB_SLOPE else ((c + SRGB_OFFSET) / (1 + SRGB_OFFSET)).pow(SRGB_GAMMA)
    }
    return LUMA_R * linear(colour shr RED_SHIFT and CHANNEL) +
        LUMA_G * linear(colour shr GREEN_SHIFT and CHANNEL) +
        LUMA_B * linear(colour and CHANNEL)
}

/** WCAG contrast ratio between two colours, 1 (none) to 21 (black on white). */
fun contrastRatio(a: Int, b: Int): Double {
    val la = relativeLuminance(a)
    val lb = relativeLuminance(b)
    return (max(la, lb) + LUMA_FLARE) / (min(la, lb) + LUMA_FLARE)
}

/**
 * [accent] made to stand out from [background] by at least [minContrast] (3:1 by default, WCAG's
 * bar for icons and other non-text marks; pass 4.5 for text): unchanged when it already does,
 * else mixed toward black or white, whichever the background contrasts with more, by the
 * smallest step that clears the bar, so the hue survives as far as it can.
 */
fun readableAccent(accent: Int, background: Int, minContrast: Double = MIN_MARK_CONTRAST): Int {
    if (contrastRatio(accent, background) >= minContrast) return accent
    val towards = if (contrastRatio(WHITE, background) >= contrastRatio(BLACK, background)) WHITE else BLACK
    return (1..MIX_STEPS).asSequence()
        .map { step -> mix(accent, towards, step / MIX_STEPS.toFloat()) }
        .firstOrNull { contrastRatio(it, background) >= minContrast }
        ?: towards
}

private fun mix(from: Int, to: Int, amount: Float): Int {
    fun channel(shift: Int): Float {
        val a = from shr shift and CHANNEL
        val b = to shr shift and CHANNEL
        return a + (b - a) * amount
    }
    return argb(channel(RED_SHIFT), channel(GREEN_SHIFT), channel(0))
}

/** Rounded, not truncated: a mean of equal channels comes out a hair under them in float. */
private fun argb(r: Float, g: Float, b: Float): Int {
    fun channel(value: Float) = value.roundToInt().coerceIn(0, CHANNEL)
    return OPAQUE or (channel(r) shl RED_SHIFT) or (channel(g) shl GREEN_SHIFT) or channel(b)
}

/** Side of the square an icon is shrunk to before its colours are counted: 576 pixels is plenty. */
const val ACCENT_SAMPLE_PX = 24

/** WCAG's minimum contrast for graphical objects. */
const val MIN_MARK_CONTRAST = 3.0

private const val HUE_BINS = 24
private const val HUE_SECTORS = 6f
private const val GREEN_SECTOR = 2f
private const val BLUE_SECTOR = 4f
private const val FULL_TURN = 360f
private const val NEIGHBOUR_SHARE = 0.5f

/** Below half opacity a pixel is anti-aliasing or shadow, not the icon. */
private const val MIN_ALPHA = 0x80

/** Channel spread under which a pixel reads as grey, and brightness under which as black. */
private const val MIN_CHROMA = 0.16f
private const val MIN_BRIGHTNESS = 0x33

/** The winning hue's vividness-weighted share of the visible pixels needed to count. */
private const val MIN_SHARE = 0.04f

private const val MIX_STEPS = 20
private const val CHANNEL = 0xFF
private const val ALPHA_SHIFT = 24
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val OPAQUE = -0x1000000
private const val WHITE = -0x1
private const val BLACK = OPAQUE

private const val SRGB_KNEE = 0.03928
private const val SRGB_SLOPE = 12.92
private const val SRGB_OFFSET = 0.055
private const val SRGB_GAMMA = 2.4
private const val LUMA_R = 0.2126
private const val LUMA_G = 0.7152
private const val LUMA_B = 0.0722
private const val LUMA_FLARE = 0.05
