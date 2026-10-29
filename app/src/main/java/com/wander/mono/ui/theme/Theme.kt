package com.wander.mono.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

/**
 * Material 3 Expressive, coloured from the wallpaper (Android 12+). `MotionScheme.expressive()` is
 * what every built-in component and every screen animation reads its springs from; swapping it for
 * [NoMotionScheme] is how reduced motion is honoured in one place. Below Android 12 there is no
 * wallpaper palette, so the Material baseline scheme is used.
 */
@Composable
fun MonoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
        colorScheme = scheme,
        motionScheme = if (reduceMotion) NoMotionScheme else MotionScheme.expressive(),
    ) {
        CompositionLocalProvider(LocalReducedMotion provides reduceMotion, content = content)
    }
}
