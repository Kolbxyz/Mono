package com.wander.mono.core.apps

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap

/**
 * An adaptive icon's background and foreground as a [sizePx] square with no mask, for the UI to
 * cut to the user's shape; null for a legacy icon (and before Android 8), whose own outline must
 * stay. Like [monochromeBitmap], only the layers' visible centre is drawn.
 */
fun fullBleedBitmap(icon: Drawable, sizePx: Int): Bitmap? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) drawFullBleed(icon, sizePx) else null

@RequiresApi(Build.VERSION_CODES.O)
private fun drawFullBleed(icon: Drawable, sizePx: Int): Bitmap? {
    val adaptive = icon as? AdaptiveIconDrawable ?: return null
    val inset = monochromeInset(sizePx, AdaptiveIconDrawable.getExtraInsetFraction())
    val bitmap = createBitmap(sizePx, sizePx)
    val canvas = Canvas(bitmap)
    listOfNotNull(adaptive.background, adaptive.foreground).forEach { layer ->
        layer.setBounds(-inset, -inset, sizePx + inset, sizePx + inset)
        layer.draw(canvas)
    }
    return bitmap
}
