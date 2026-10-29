package com.wander.mono.core.media

import android.graphics.Bitmap

/** Remembers the colour of the last artwork, so play/pause callbacks do not resample the same bitmap. */
internal class ArtMemo {
    private var lastArt: Bitmap? = null
    private var lastColor: Int = 0

    fun colorOf(art: Bitmap): Int {
        if (art !== lastArt) {
            lastColor = averageColor(art)
            lastArt = art
        }
        return lastColor
    }
}
