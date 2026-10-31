package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.BadgeSettings
import com.wander.mono.data.settings.LiveActivitySettings
import com.wander.mono.data.settings.PreviewSettings

/**
 * How an app with notifications shows it on home: a count (or a dot), its latest one underneath,
 * and which ongoing ones become live rows under it.
 */
@Composable
fun NotificationSection(settings: SettingsViewModel) {
    SettingsGroup(R.string.settings_notifications) {
        SwitchSetting(
            settings,
            BadgeSettings.ShowCounts,
            title = R.string.settings_badge_counts,
            icon = R.drawable.sym_counter_1,
        )
        SwitchSetting(
            settings,
            PreviewSettings.ShowPreviews,
            title = R.string.settings_previews_show,
            icon = R.drawable.sym_chat,
        )
        ToggleChipsSetting(
            settings,
            title = R.string.settings_live_rows,
            icon = R.drawable.sym_hourglass_bottom,
            chips = listOf(
                ToggleChip(LiveActivitySettings.LiveUpdates, R.string.settings_live_updates, R.drawable.sym_download),
                ToggleChip(LiveActivitySettings.Timers, R.string.settings_live_timers, R.drawable.sym_timer),
                ToggleChip(
                    LiveActivitySettings.Navigation,
                    R.string.settings_live_navigation,
                    R.drawable.sym_navigation,
                ),
            ),
        )
    }
}
