package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.PreviewSettings

/** Whether favourites show their latest notification underneath. */
@Composable
fun PreviewSection(settings: SettingsViewModel) {
    SettingsGroup(R.string.settings_previews) {
        SwitchSetting(
            settings,
            PreviewSettings.ShowPreviews,
            title = R.string.settings_previews_show,
            summary = R.string.settings_previews_show_summary,
        )
    }
}
