package com.wander.mono.core.apps

import androidx.compose.ui.graphics.ImageBitmap

/**
 * A decoded app icon. A [isMonochrome] bitmap is an untinted glyph that the UI tints with a colour
 * role at draw time, so a theme or wallpaper change recolours it without decoding it again. A
 * [isFullBleed] bitmap is an adaptive icon without the system's mask, for the UI to cut to the
 * user's icon shape. [accent] is the app's colour as its full-colour icon shows it
 * ([dominantAccent], raw: make it readable against its background with [readableAccent]); null
 * when that icon has too little colour to have one, and for shortcut icons, which are not read.
 */
class AppIconImage(
    val bitmap: ImageBitmap,
    val isMonochrome: Boolean,
    val isFullBleed: Boolean = false,
    val accent: Int? = null,
)
