package com.wander.mono.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.wander.mono.data.settings.ThemePalette

/**
 * One mode's hand-picked colour roles, as ARGB. The roles left out follow from these: background
 * is the surface, the surface tint the primary, the variant surface the highest container; the
 * inverse roles come from the other mode; error keeps Material's own red.
 */
internal data class SchemeTones(
    val primary: Long,
    val onPrimary: Long,
    val primaryContainer: Long,
    val onPrimaryContainer: Long,
    val secondary: Long,
    val onSecondary: Long,
    val secondaryContainer: Long,
    val onSecondaryContainer: Long,
    val tertiary: Long,
    val onTertiary: Long,
    val tertiaryContainer: Long,
    val onTertiaryContainer: Long,
    val surfaceDim: Long,
    val surface: Long,
    val surfaceBright: Long,
    val surfaceContainerLowest: Long,
    val surfaceContainerLow: Long,
    val surfaceContainer: Long,
    val surfaceContainerHigh: Long,
    val surfaceContainerHighest: Long,
    val onSurface: Long,
    val onSurfaceVariant: Long,
    val outline: Long,
    val outlineVariant: Long,
)

/** A hand-made palette: its light and dark roles, which each scheme also reads its inverse and fixed roles from. */
internal class HandPalette(val light: SchemeTones, val dark: SchemeTones) {
    fun scheme(darkTheme: Boolean): ColorScheme {
        val own = if (darkTheme) dark else light
        val other = if (darkTheme) light else dark
        val base = if (darkTheme) darkColorScheme() else lightColorScheme()
        return base.withAccents(own).withSurfaces(own, other).withFixed(light, dark)
    }
}

/** [this] palette's hand-made palette; null for the wallpaper's. */
internal fun ThemePalette.handPalette(): HandPalette? = when (this) {
    ThemePalette.Wallpaper -> null
    ThemePalette.Graphite -> GraphitePalette
    ThemePalette.Sand -> SandPalette
    ThemePalette.Sage -> SagePalette
    ThemePalette.Ocean -> OceanPalette
    ThemePalette.Dusk -> DuskPalette
    ThemePalette.Ember -> EmberPalette
}

/**
 * [this] palette's scheme in light or [dark]: a hand-made one, or the wallpaper's (Android 12+;
 * Material's baseline before, which has no wallpaper colours).
 */
@Composable
fun ThemePalette.colorScheme(dark: Boolean): ColorScheme {
    val context = LocalContext.current
    val hand = remember(this, dark) { handPalette()?.scheme(dark) }
    return hand ?: wallpaperScheme(context, dark)
}

private fun wallpaperScheme(context: Context, dark: Boolean): ColorScheme = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dark -> dynamicDarkColorScheme(context)
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
    dark -> darkColorScheme()
    else -> lightColorScheme()
}

private fun ColorScheme.withAccents(tones: SchemeTones) = copy(
    primary = Color(tones.primary),
    onPrimary = Color(tones.onPrimary),
    primaryContainer = Color(tones.primaryContainer),
    onPrimaryContainer = Color(tones.onPrimaryContainer),
    secondary = Color(tones.secondary),
    onSecondary = Color(tones.onSecondary),
    secondaryContainer = Color(tones.secondaryContainer),
    onSecondaryContainer = Color(tones.onSecondaryContainer),
    tertiary = Color(tones.tertiary),
    onTertiary = Color(tones.onTertiary),
    tertiaryContainer = Color(tones.tertiaryContainer),
    onTertiaryContainer = Color(tones.onTertiaryContainer),
    surfaceTint = Color(tones.primary),
)

/** [other] is the opposite mode, whose surface and text are this mode's inverse ones. */
private fun ColorScheme.withSurfaces(tones: SchemeTones, other: SchemeTones) = copy(
    background = Color(tones.surface),
    onBackground = Color(tones.onSurface),
    surface = Color(tones.surface),
    onSurface = Color(tones.onSurface),
    surfaceVariant = Color(tones.surfaceContainerHighest),
    onSurfaceVariant = Color(tones.onSurfaceVariant),
    surfaceDim = Color(tones.surfaceDim),
    surfaceBright = Color(tones.surfaceBright),
    surfaceContainerLowest = Color(tones.surfaceContainerLowest),
    surfaceContainerLow = Color(tones.surfaceContainerLow),
    surfaceContainer = Color(tones.surfaceContainer),
    surfaceContainerHigh = Color(tones.surfaceContainerHigh),
    surfaceContainerHighest = Color(tones.surfaceContainerHighest),
    outline = Color(tones.outline),
    outlineVariant = Color(tones.outlineVariant),
    inversePrimary = Color(other.primary),
    inverseSurface = Color(other.surfaceContainerHighest),
    inverseOnSurface = Color(other.onSurface),
)

/**
 * The fixed roles stay the same in both modes: the light container tones and the dark accents,
 * as Material defines them, so a fixed-colour component never shows the baseline purple.
 */
private fun ColorScheme.withFixed(light: SchemeTones, dark: SchemeTones) = copy(
    primaryFixed = Color(light.primaryContainer),
    primaryFixedDim = Color(dark.primary),
    onPrimaryFixed = Color(dark.onPrimary),
    onPrimaryFixedVariant = Color(dark.primaryContainer),
    secondaryFixed = Color(light.secondaryContainer),
    secondaryFixedDim = Color(dark.secondary),
    onSecondaryFixed = Color(dark.onSecondary),
    onSecondaryFixedVariant = Color(dark.secondaryContainer),
    tertiaryFixed = Color(light.tertiaryContainer),
    tertiaryFixedDim = Color(dark.tertiary),
    onTertiaryFixed = Color(dark.onTertiary),
    onTertiaryFixedVariant = Color(dark.tertiaryContainer),
)
