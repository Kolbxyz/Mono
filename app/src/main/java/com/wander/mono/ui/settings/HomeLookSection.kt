package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.HomeLookSettings
import com.wander.mono.ui.home.densityLabel

/**
 * How roomy home's rows are and where the clock sits. Where a short list sits is set from edit
 * mode's toolbar, next to the list it moves.
 */
@Composable
fun HomeLookSection(settings: SettingsViewModel) {
    SettingsGroup(R.string.settings_home_look) {
        ChoiceSetting(settings, HomeLookSettings.Density, R.string.settings_row_density, R.drawable.sym_view_agenda) {
            densityLabel(it)
        }
        SwitchSetting(
            settings,
            HomeLookSettings.Reachable,
            title = R.string.settings_reachable_layout,
            icon = R.drawable.sym_swipe,
        )
    }
}
