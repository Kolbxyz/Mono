package com.wander.mono.core.apps

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import kotlin.math.roundToInt

/**
 * The icon's monochrome layer as a [sizePx] square bitmap, or null when the app ships none (and
 * always before Android 13, which introduced the layer). Only the layer's visible centre is drawn,
 * as the system's icon mask would show it, so the glyph fills the square like a normal icon does.
 */
fun monochromeBitmap(icon: Drawable, sizePx: Int): Bitmap? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) drawMonochrome(icon, sizePx) else null

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun drawMonochrome(icon: Drawable, sizePx: Int): Bitmap? {
    val layer = (icon as? AdaptiveIconDrawable)?.monochrome ?: return null
    val inset = monochromeInset(sizePx, AdaptiveIconDrawable.getExtraInsetFraction())
    val bitmap = createBitmap(sizePx, sizePx)
    layer.setBounds(-inset, -inset, sizePx + inset, sizePx + inset)
    layer.draw(Canvas(bitmap))
    return bitmap
}

/**
 * How far an adaptive layer overflows each edge of its visible [sizePx] square: layers are 108 dp
 * with a 72 dp visible centre, an overflow of [extraInsetFraction] (0.25) of the visible size.
 */
fun monochromeInset(sizePx: Int, extraInsetFraction: Float): Int = (sizePx * extraInsetFraction).roundToInt()
