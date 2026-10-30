package com.wander.mono.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.wander.mono.data.settings.IconShape
import com.wander.mono.data.settings.ThemeMode

/**
 * Material 3 Expressive, coloured from the wallpaper (Android 12+). `MotionScheme.expressive()` is
 * what every built-in component and every screen animation reads its springs from; swapping it for
 * [NoMotionScheme] is how reduced motion is honoured in one place. Below Android 12 there is no
 * wallpaper palette, so the Material baseline scheme is used. [amoledBlack] turns the dark
 * surfaces pure black and leaves the accents alone. [iconShape] is what app icons are cut to
 * ([LocalIconMask]).
 */
@Composable
fun MonoTheme(
    darkTheme: Boolean,
    amoledBlack: Boolean,
    iconShape: IconShape,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val reduceMotion = rememberSystemAnimationsDisabled()
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialExpressiveTheme(
        colorScheme = if (darkTheme && amoledBlack) scheme.withBlackSurfaces() else scheme,
        motionScheme = if (reduceMotion) NoMotionScheme else MotionScheme.expressive(),
        shapes = MonoShapes,
        typography = MonoTypography,
    ) {
        CompositionLocalProvider(
            LocalReducedMotion provides reduceMotion,
            LocalIconMask provides iconShape.toMask(),
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
