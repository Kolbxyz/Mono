package com.wander.mono.data.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Light or dark: following the system, or fixed. */
enum class ThemeMode(override val stored: String) : StoredEnum {
    System("system"),
    Light("light"),
    Dark("dark"),
}

/**
 * Where the colours come from: the wallpaper (Android 12+; Material's baseline before), or one of
 * Mono's hand-made palettes, each with a light and a dark scheme.
 */
enum class ThemePalette(override val stored: String) : StoredEnum {
    Wallpaper("wallpaper"),
    Graphite("graphite"),
    Sand("sand"),
    Sage("sage"),
    Ocean("ocean"),
    Dusk("dusk"),
    Ember("ember"),
}

object ThemeSettings {
    val Mode = EnumSetting("theme_mode", ThemeMode.System, ThemeMode.entries)

    val Palette = EnumSetting("theme_palette", ThemePalette.Wallpaper, ThemePalette.entries)

    /** Pure black surfaces in dark theme, for OLED screens; accents keep the palette's colours. */
    val AmoledBlack = BooleanSetting("theme_amoled_black", default = false)

    /** With [AmoledBlack] in dark theme, the window is black instead of the wallpaper. */
    val BlackWallpaper = BooleanSetting("theme_black_wallpaper", default = false)
}

/** What `MonoTheme` and the window need, read together so the theme changes in one step. */
data class ThemeChoice(
    val mode: ThemeMode,
    val palette: ThemePalette,
    val amoledBlack: Boolean,
    val blackWallpaper: Boolean,
    val iconShape: IconShape,
    val iconTheming: IconTheming,
)

fun SettingsStore.themeChoice(): Flow<ThemeChoice> = combine(
    combine(this[ThemeSettings.Mode], this[ThemeSettings.Palette], ::Pair),
    this[ThemeSettings.AmoledBlack],
    this[ThemeSettings.BlackWallpaper],
    this[IconSettings.Shape],
    combine(this[IconSettings.ThemedTint], this[IconSettings.TintRows], ::IconTheming),
) { (mode, palette), amoledBlack, blackWallpaper, iconShape, iconTheming ->
    ThemeChoice(mode, palette, amoledBlack, blackWallpaper, iconShape, iconTheming)
}
