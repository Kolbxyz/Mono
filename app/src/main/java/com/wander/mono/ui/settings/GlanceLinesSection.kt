package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.ClockSettings

/** Which lines the clock shows under the time; each still shows only when it has something to say. */
@Composable
fun GlanceLinesSection(settings: SettingsViewModel) {
    SettingsGroup(R.string.settings_glance_lines) {
        SwitchSetting(
            settings,
            ClockSettings.ShowDate,
            title = R.string.settings_glance_date,
            icon = R.drawable.sym_today,
        )
        SwitchSetting(
            settings,
            ClockSettings.ShowAlarm,
            title = R.string.settings_glance_alarm,
            summary = R.string.settings_glance_alarm_short,
            icon = R.drawable.sym_alarm,
        )
        SwitchSetting(
            settings,
            ClockSettings.ShowEvent,
            title = R.string.settings_glance_event,
            summary = R.string.settings_glance_event_short,
            icon = R.drawable.sym_event,
        )
        SwitchSetting(
            settings,
            ClockSettings.ShowBattery,
            title = R.string.settings_glance_battery,
            summary = R.string.settings_glance_battery_short,
            icon = R.drawable.sym_battery_full,
        )
    }
}
