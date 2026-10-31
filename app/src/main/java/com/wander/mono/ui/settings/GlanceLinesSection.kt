package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.ClockSettings

/** Which lines the clock shows under the time; each still shows only when it has something to say. */
@Composable
fun GlanceLinesSection(settings: SettingsViewModel) {
    SettingsGroup(R.string.settings_glance_lines) {
        ToggleChipsSetting(
            settings,
            title = R.string.settings_glance_shows,
            chips = listOf(
                ToggleChip(ClockSettings.ShowDate, R.string.settings_glance_date, R.drawable.sym_today),
                ToggleChip(ClockSettings.ShowAlarm, R.string.settings_glance_alarm, R.drawable.sym_alarm),
                ToggleChip(ClockSettings.ShowEvent, R.string.settings_glance_event, R.drawable.sym_event),
                ToggleChip(ClockSettings.ShowBattery, R.string.settings_glance_battery, R.drawable.sym_battery_full),
            ),
        )
    }
}
