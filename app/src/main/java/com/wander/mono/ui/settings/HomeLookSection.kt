package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.HomeLookSettings

/** How home stays readable over the wallpaper. */
@Composable
fun HomeLookSection(settings: SettingsViewModel) {
    SettingsGroup(R.string.settings_home_look) {
        SwitchSetting(
            settings,
            HomeLookSettings.TextShadow,
            title = R.string.settings_text_shadow,
            summary = R.string.settings_text_shadow_short,
            icon = R.drawable.sym_ev_shadow,
        )
    }
}
