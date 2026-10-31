package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.ThemeMode
import com.wander.mono.data.settings.ThemeSettings
import com.wander.mono.ui.theme.isDark

/**
 * Light, dark or following the system; the colours, from the wallpaper or a hand-made palette;
 * and pure black for OLED screens. With pure black, the wallpaper itself can go black too.
 */
@Composable
fun ThemeSection(settings: SettingsViewModel) {
    val amoled = settings.collect(ThemeSettings.AmoledBlack)
    val dark = settings.collect(ThemeSettings.Mode)?.isDark()
    SettingsGroup(R.string.settings_theme) {
        ChoiceSetting(settings, ThemeSettings.Mode, R.string.settings_theme_mode, R.drawable.sym_dark_mode) { mode ->
            when (mode) {
                ThemeMode.System -> R.string.settings_theme_system
                ThemeMode.Light -> R.string.settings_theme_light
                ThemeMode.Dark -> R.string.settings_theme_dark
            }
        }
        if (dark != null) PaletteRow(settings, dark)
        SwitchSetting(
            settings,
            ThemeSettings.AmoledBlack,
            title = R.string.settings_amoled_black,
            icon = R.drawable.sym_invert_colors,
        )
        if (amoled == true) {
            SwitchSetting(
                settings,
                ThemeSettings.BlackWallpaper,
                title = R.string.settings_black_wallpaper,
                icon = R.drawable.sym_contrast,
            )
        }
    }
}
