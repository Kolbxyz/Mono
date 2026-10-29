package com.wander.mono.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSp

/**
 * Whether the user's font scale is large enough (over 1.5×, spec 9) that a row should stack its
 * controls under its text instead of beside it.
 */
@Composable
@ReadOnlyComposable
fun isLargeFontScale(): Boolean = LocalDensity.current.fontScale > STACKED_FONT_SCALE

/**
 * This style, grown with the font scale only up to [max] times its unscaled size (the clock caps
 * at 1.3×, spec 9, so the list stays in view). Measured in pixels, so Android 14's non-linear
 * font scaling, which already grows large text less, is capped only when it actually exceeds [max].
 */
@Composable
@ReadOnlyComposable
fun TextStyle.capFontScale(max: Float): TextStyle {
    val density = LocalDensity.current
    fun capped(size: TextUnit): TextUnit = with(density) {
        if (!size.isSp) return size
        val limit = (size.value * max).dp
        if (size.toPx() <= limit.toPx()) size else limit.toSp()
    }
    return copy(fontSize = capped(fontSize), lineHeight = capped(lineHeight))
}

/** Above this font scale, live rows stack their controls under their text. */
const val STACKED_FONT_SCALE = 1.5f

/** The clock grows with the font scale up to this, then stops. */
const val CLOCK_MAX_FONT_SCALE = 1.3f
