package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.ThemeMode
import com.wander.mono.data.settings.ThemeSettings

/** Light, dark or following the system, and pure black for OLED screens. */
@Composable
fun ThemeSection(settings: SettingsViewModel) {
    SettingsGroup(R.string.settings_theme) {
        ChoiceSetting(settings, ThemeSettings.Mode, R.string.settings_theme_mode, R.drawable.sym_dark_mode) { mode ->
            when (mode) {
                ThemeMode.System -> R.string.settings_theme_system
                ThemeMode.Light -> R.string.settings_theme_light
                ThemeMode.Dark -> R.string.settings_theme_dark
            }
        }
        SwitchSetting(
            settings,
            ThemeSettings.AmoledBlack,
            title = R.string.settings_amoled_black,
            icon = R.drawable.sym_invert_colors,
        )
    }
}
