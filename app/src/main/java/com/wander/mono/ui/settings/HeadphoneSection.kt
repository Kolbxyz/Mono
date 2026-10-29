package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.HeadphoneSettings

/** Headphones: whether the apps chosen from their long-press options move to the top of home. */
@Composable
fun HeadphoneSection(settings: SettingsViewModel) {
    SettingsGroup(R.string.settings_headphones) {
        SwitchSetting(
            settings,
            HeadphoneSettings.LiftApps,
            title = R.string.settings_headphones_lift,
            summary = R.string.settings_headphones_lift_summary,
        )
    }
}
