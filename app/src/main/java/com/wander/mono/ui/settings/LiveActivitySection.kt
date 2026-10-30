package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.LiveActivitySettings

/** Which ongoing notifications show as live rows under their favourite. */
@Composable
fun LiveActivitySection(settings: SettingsViewModel) {
    SettingsGroup(R.string.settings_live_rows) {
        SwitchSetting(
            settings,
            LiveActivitySettings.LiveUpdates,
            title = R.string.settings_live_updates,
            summary = R.string.settings_live_updates_summary,
            icon = R.drawable.sym_hourglass_bottom,
        )
        SwitchSetting(
            settings,
            LiveActivitySettings.Timers,
            title = R.string.settings_live_timers,
            icon = R.drawable.sym_timer,
        )
        SwitchSetting(
            settings,
            LiveActivitySettings.Navigation,
            title = R.string.settings_live_navigation,
            icon = R.drawable.sym_navigation,
        )
    }
}
