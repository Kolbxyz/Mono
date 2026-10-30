package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.settings.AccessibilitySettings

/**
 * Readability on top of what Android's own accessibility settings already give: the large, simple
 * preset first, as the one tap most people here are looking for, then high-contrast text alone.
 */
@Composable
fun AccessibilitySection(settings: SettingsViewModel, simple: SimpleModeViewModel = viewModel()) {
    SettingsGroup(R.string.settings_accessibility) {
        val initial = remember(simple) { simple.current() }
        val simpleOn = simple.on.collectAsStateWithLifecycle(initial).value
        if (simpleOn != null) {
            SwitchRow(
                simpleOn,
                simple::setOn,
                title = R.string.settings_simple_mode,
                summary = R.string.settings_simple_mode_short,
                icon = R.drawable.sym_visibility,
            )
        }
        SwitchSetting(
            settings,
            AccessibilitySettings.HighContrastText,
            title = R.string.settings_high_contrast_text,
            summary = R.string.settings_high_contrast_short,
            icon = R.drawable.sym_contrast,
        )
    }
}
