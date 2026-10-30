package com.wander.mono.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.wander.mono.data.settings.IconTheming

/**
 * How icons and rows are coloured: the themed glyphs' tint and whether rows take their app's
 * colour. Provided by `MonoTheme`; read through `AppIcon` and `rememberRowAccent`, not directly.
 * Static: it changes only when the user changes a setting.
 */
val LocalIconTheming = staticCompositionLocalOf { IconTheming.Default }
