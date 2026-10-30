package com.wander.mono.core.apps

import androidx.compose.ui.graphics.ImageBitmap

/**
 * A decoded app icon. A [isMonochrome] bitmap is an untinted glyph that the UI tints with a colour
 * role at draw time, so a theme or wallpaper change recolours it without decoding it again. A
 * [isFullBleed] bitmap is an adaptive icon without the system's mask, for the UI to cut to the
 * user's icon shape.
 */
class AppIconImage(val bitmap: ImageBitmap, val isMonochrome: Boolean, val isFullBleed: Boolean = false)
