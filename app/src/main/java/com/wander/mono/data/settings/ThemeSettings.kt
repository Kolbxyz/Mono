package com.wander.mono.data.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Light or dark: following the system, or fixed. */
enum class ThemeMode(override val stored: String) : StoredEnum {
    System("system"),
    Light("light"),
    Dark("dark"),
}

object ThemeSettings {
    val Mode = EnumSetting("theme_mode", ThemeMode.System, ThemeMode.entries)

    /** Pure black surfaces in dark theme, for OLED screens; accents keep the wallpaper colours. */
    val AmoledBlack = BooleanSetting("theme_amoled_black", default = false)

    /** With [AmoledBlack] in dark theme, the window is black instead of the wallpaper. */
    val BlackWallpaper = BooleanSetting("theme_black_wallpaper", default = false)
}

/** What `MonoTheme` and the window need, read together so the theme changes in one step. */
data class ThemeChoice(
    val mode: ThemeMode,
    val amoledBlack: Boolean,
    val blackWallpaper: Boolean,
    val iconShape: IconShape,
    val iconTheming: IconTheming,
)

fun SettingsStore.themeChoice(): Flow<ThemeChoice> = combine(
    this[ThemeSettings.Mode],
    this[ThemeSettings.AmoledBlack],
    this[ThemeSettings.BlackWallpaper],
    this[IconSettings.Shape],
    combine(this[IconSettings.ThemedTint], this[IconSettings.TintRows], ::IconTheming),
    ::ThemeChoice,
)
