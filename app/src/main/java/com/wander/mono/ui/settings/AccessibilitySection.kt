package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.AccessibilitySettings

/** Readability on top of what Android's own accessibility settings already give. */
@Composable
fun AccessibilitySection(settings: SettingsViewModel) {
    SettingsGroup(R.string.settings_accessibility) {
        SwitchSetting(
            settings,
            AccessibilitySettings.HighContrastText,
            title = R.string.settings_high_contrast_text,
            summary = R.string.settings_high_contrast_short,
            icon = R.drawable.sym_contrast,
        )
    }
}
