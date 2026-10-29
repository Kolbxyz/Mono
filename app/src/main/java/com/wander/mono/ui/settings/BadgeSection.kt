package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.BadgeSettings

/** How apps with notifications are marked, on home and in the drawer. */
@Composable
fun BadgeSection(settings: SettingsViewModel) {
    SettingsGroup(R.string.settings_badges) {
        SwitchSetting(
            settings,
            BadgeSettings.ShowCounts,
            title = R.string.settings_badge_counts,
            summary = R.string.settings_badge_counts_summary,
        )
    }
}
