package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.BadgeSettings
import com.wander.mono.data.settings.PreviewSettings

/** How an app with notifications shows it: a count or a dot, and its latest one underneath. */
@Composable
fun NotificationSection(settings: SettingsViewModel) {
    SettingsGroup(R.string.settings_badges_previews) {
        SwitchSetting(
            settings,
            BadgeSettings.ShowCounts,
            title = R.string.settings_badge_counts,
            summary = R.string.settings_badge_counts_summary,
            icon = R.drawable.sym_counter_1,
        )
        SwitchSetting(
            settings,
            PreviewSettings.ShowPreviews,
            title = R.string.settings_previews_show,
            summary = R.string.settings_previews_show_short,
            icon = R.drawable.sym_chat,
        )
    }
}
