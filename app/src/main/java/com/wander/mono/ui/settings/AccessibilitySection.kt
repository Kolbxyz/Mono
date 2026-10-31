package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.settings.AccessibilitySettings
import com.wander.mono.data.settings.HomeLookSettings
import com.wander.mono.data.settings.LABEL_SCALE_STEP

/**
 * Readability on top of what Android's own accessibility settings already give: the large, simple
 * preset first, as the one tap most people here are looking for, then its parts alone (label size,
 * high contrast) and the text shadow that keeps the wallpaper undimmed.
 */
@Composable
fun AccessibilitySection(settings: SettingsViewModel, simple: SimpleModeViewModel = viewModel()) {
    SettingsGroup(R.string.settings_accessibility) {
        val initial = remember(simple) { simple.current() }
        val simpleOn = simple.on.collectAsStateWithLifecycle(initial).value
        if (simpleOn != null) {
            SwitchRow(simpleOn, simple::setOn, title = R.string.settings_simple_mode, icon = R.drawable.sym_visibility)
        }
        SliderSetting(
            settings,
            HomeLookSettings.LabelScale,
            title = R.string.settings_label_scale,
            step = LABEL_SCALE_STEP,
            icon = R.drawable.sym_format_size,
        ) { stringResource(R.string.settings_label_scale_value, it) }
        SwitchSetting(
            settings,
            AccessibilitySettings.HighContrastText,
            title = R.string.settings_high_contrast_text,
            icon = R.drawable.sym_contrast,
        )
        SwitchSetting(
            settings,
            HomeLookSettings.TextShadow,
            title = R.string.settings_text_shadow,
            icon = R.drawable.sym_ev_shadow,
        )
    }
}
