package com.wander.mono.ui.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.settings.BooleanSetting
import com.wander.mono.data.settings.ScreenTimeSettings
import com.wander.mono.data.settings.UsageSettings

/**
 * Opt-in screen time, both rows reading usage access: today's time in an app's long-press options,
 * and the nudge that fades a favourite past the daily limit set in its options. Turning either on
 * without the access opens Android's page to grant it; until then the row says so.
 */
@Composable
fun ScreenTimeSection(settings: SettingsViewModel, access: UsageAccessViewModel = viewModel()) {
    val granted by access.granted.collectAsStateWithLifecycle()
    LifecycleStartEffect(access) {
        access.recheck()
        onStopOrDispose {}
    }
    SettingsGroup(R.string.settings_screen_time) {
        UsageSwitch(
            settings,
            access,
            granted,
            UsageSettings.ShowInSheet,
            title = R.string.settings_usage_in_sheet,
            summary = R.string.settings_usage_short,
            icon = R.drawable.sym_hourglass_bottom,
        )
        UsageSwitch(
            settings,
            access,
            granted,
            ScreenTimeSettings.Nudge,
            title = R.string.settings_screen_time_nudge,
            summary = R.string.settings_screen_time_short,
            icon = R.drawable.sym_timer,
        )
    }
}

/** An on/off row that needs usage access: on only while granted, its summary asking for it until then. */
@Composable
@Suppress("LongParameterList")
private fun UsageSwitch(
    settings: SettingsViewModel,
    access: UsageAccessViewModel,
    granted: Boolean,
    setting: BooleanSetting,
    @StringRes title: Int,
    @StringRes summary: Int,
    @DrawableRes icon: Int,
) {
    val on = settings.collect(setting) ?: return
    SwitchRow(
        checked = on && granted,
        onCheckedChange = { checked ->
            settings.set(setting, checked)
            if (checked && !granted) access.openSettings()
        },
        title = title,
        summary = if (on && !granted) R.string.settings_usage_grant_short else summary,
        icon = icon,
    )
}
