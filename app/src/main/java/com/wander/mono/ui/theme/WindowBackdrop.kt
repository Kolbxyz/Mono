package com.wander.mono.ui.theme

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.WindowManager

/**
 * The wallpaper behind Mono's window (`Theme.Mono`'s default), or, for "solid black wallpaper",
 * an opaque black window: with the wallpaper flag cleared the system stops drawing the wallpaper
 * behind Mono altogether, which on OLED leaves those pixels off.
 */
fun Window.showWallpaper(show: Boolean) {
    if (show) {
        addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    } else {
        clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        setBackgroundDrawable(ColorDrawable(Color.BLACK))
    }
}
