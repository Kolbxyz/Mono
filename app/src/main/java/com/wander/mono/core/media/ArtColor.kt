package com.wander.mono.core.media

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.scale

private const val SAMPLE_SIZE = 24

/** The average colour of [art], over a small downscaled copy (plain bilinear scaling to 1 px skips most pixels). */
internal fun averageColor(art: Bitmap): Int {
    // Hardware bitmaps cannot be read back pixel by pixel.
    val readable = if (art.config == Bitmap.Config.HARDWARE) art.copy(Bitmap.Config.ARGB_8888, false) else art
    val sample = readable.scale(SAMPLE_SIZE, SAMPLE_SIZE, filter = true)
    val pixels = IntArray(SAMPLE_SIZE * SAMPLE_SIZE)
    sample.getPixels(pixels, 0, SAMPLE_SIZE, 0, 0, SAMPLE_SIZE, SAMPLE_SIZE)
    val red = pixels.sumOf { Color.red(it) } / pixels.size
    val green = pixels.sumOf { Color.green(it) } / pixels.size
    val blue = pixels.sumOf { Color.blue(it) } / pixels.size
    return Color.rgb(red, green, blue)
}
