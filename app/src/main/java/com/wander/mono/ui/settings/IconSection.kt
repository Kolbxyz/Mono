package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.IconMode
import com.wander.mono.data.settings.IconSettings

/** Full-colour, themed monochrome, or no app icons at all. */
@Composable
fun IconSection(settings: SettingsViewModel) {
    SettingsGroup(R.string.settings_icons) {
        ChoiceSetting(settings, IconSettings.Mode, R.string.settings_icon_mode) { mode ->
            when (mode) {
                IconMode.Colour -> R.string.settings_icon_colour
                IconMode.Themed -> R.string.settings_icon_themed
                IconMode.TextOnly -> R.string.settings_icon_text_only
            }
        }
    }
}
