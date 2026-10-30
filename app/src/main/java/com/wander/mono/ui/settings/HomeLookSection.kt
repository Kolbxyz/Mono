package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.HomeLookSettings
import com.wander.mono.ui.home.densityLabel

/** How home stays readable over the wallpaper, and how roomy its rows are. */
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
        ChoiceSetting(settings, HomeLookSettings.Density, R.string.settings_row_density, R.drawable.sym_view_agenda) {
            densityLabel(it)
        }
    }
}
