package com.wander.mono.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.wander.mono.data.settings.IconShape
import com.wander.mono.data.settings.IconTheming
import com.wander.mono.data.settings.ThemeMode
import com.wander.mono.data.settings.ThemePalette

/**
 * Material 3 Expressive, coloured from the wallpaper (Android 12+) or one of Mono's hand-made
 * palettes ([palette], see [colorScheme]). `MotionScheme.expressive()` is what every built-in
 * component and every screen animation reads its springs from; swapping it for [NoMotionScheme] is
 * how reduced motion is honoured in one place. Below Android 12 there is no wallpaper palette, so
 * the wallpaper choice falls back to Material's baseline scheme. [amoledBlack] turns the dark
 * surfaces pure black and leaves the accents alone. [iconShape] is what app icons are cut to
 * ([LocalIconMask]); [iconTheming] how themed glyphs and rows are coloured ([LocalIconTheming]).
 *
 * The design tokens, for anything new:
 * - Type: [MonoTypography]. Every style has an `…Emphasized` twin, one weight heavier in the same
 *   family, for the one line on a surface that should lead: a header, a now-playing title, a
 *   number that matters. Never two emphasized lines side by side.
 * - Shape: [MonoShapes], `extraSmall` 8 dp up to `extraExtraLarge` 52 dp; rows and cards take
 *   `large`, sheets `extraLarge`. A press tightens corners (`expressivePress`), never rounds them.
 * - Motion: `MaterialTheme.motionScheme` only; fast spatial for presses, default spatial for
 *   layout, effects specs for fades. Indicators that loop read [LocalReducedMotion] themselves.
 */
@Composable
fun MonoTheme(
    darkTheme: Boolean,
    amoledBlack: Boolean,
    palette: ThemePalette,
    iconShape: IconShape,
    iconTheming: IconTheming,
    content: @Composable () -> Unit,
) {
    val reduceMotion = rememberSystemAnimationsDisabled()
    val scheme = palette.colorScheme(darkTheme)
    MaterialExpressiveTheme(
        colorScheme = if (darkTheme && amoledBlack) scheme.withBlackSurfaces() else scheme,
        motionScheme = if (reduceMotion) NoMotionScheme else MotionScheme.expressive(),
        shapes = MonoShapes,
        typography = MonoTypography,
    ) {
        CompositionLocalProvider(
            LocalReducedMotion provides reduceMotion,
            LocalIconMask provides iconShape.toMask(),
            LocalIconTheming provides iconTheming,
            content = content,
        )
    }
}

/** Whether [ThemeMode] means dark right now. */
@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}

/**
 * The page-level surfaces go black; the raised containers (sheets, cards, fields) keep their tone
 * so they still stand apart from the page.
 */
private fun ColorScheme.withBlackSurfaces() = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
)
