package com.wander.mono.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.calendar.CalendarEvents
import com.wander.mono.core.calendar.headerEvent
import com.wander.mono.core.quiet.QuietMode
import com.wander.mono.core.system.Battery
import com.wander.mono.core.system.SystemTime
import com.wander.mono.data.settings.QuietSettings
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.data.settings.clockLook
import com.wander.mono.ui.home.Glance
import com.wander.mono.ui.home.QuietGlance
import com.wander.mono.ui.home.alarmWithinDay
import com.wander.mono.ui.home.glanceBattery
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * The live clock preview at the top of the Home page: the real time, alarm, next event, battery
 * and quiet state, in the clock settings as they are being changed. Null until first read.
 */
@HiltViewModel
class ClockPreviewViewModel @Inject constructor(
    time: SystemTime,
    battery: Battery,
    calendar: CalendarEvents,
    quiet: QuietMode,
    settings: SettingsStore,
) : ViewModel() {
    val glance: StateFlow<Glance?> = combine(
        combine(time.now, time.nextAlarm, calendar.agenda) { now, alarm, agenda ->
            Glance(now, alarmWithinDay(alarm, now), agenda?.let { headerEvent(it.events, now) })
        },
        battery.state,
        combine(quiet.enabled, settings[QuietSettings.Until]) { on, until -> if (on) QuietGlance(until) else null },
        settings.clockLook(),
    ) { base, power, quietGlance, look ->
        base.copy(battery = glanceBattery(power), quiet = quietGlance, look = look)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
