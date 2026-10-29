package com.wander.mono.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.calendar.CalendarActions
import com.wander.mono.core.calendar.CalendarEvent
import com.wander.mono.core.system.ClockActions
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/** Where the glance header's lines lead when tapped (spec 4.2), and the Quiet chip's two actions. */
@HiltViewModel
class GlanceActionsViewModel @Inject constructor(
    private val clock: ClockActions,
    private val calendar: CalendarActions,
    private val quietTimer: QuietTimer,
    private val messages: UserMessages,
) : ViewModel() {
    fun openAlarms() {
        messages.report(clock.openAlarms())
    }

    fun openCalendarAt(millis: Long) {
        messages.report(clock.openCalendarAt(millis))
    }

    fun openEvent(event: CalendarEvent) {
        messages.report(calendar.open(event))
    }

    fun endQuiet() {
        viewModelScope.launch { quietTimer.endNow() }
    }

    fun extendQuiet() {
        viewModelScope.launch { quietTimer.extendByHour(System.currentTimeMillis()) }
    }
}

/** The header's tap targets; the settings preview passes none, so nothing there opens an app. */
class GlanceTaps(
    val onTime: () -> Unit,
    val onDate: (Long) -> Unit,
    val onEvent: (CalendarEvent) -> Unit,
    val onAlarm: () -> Unit,
    val onEndQuiet: () -> Unit,
    val onExtendQuiet: () -> Unit,
)

fun GlanceActionsViewModel.taps(): GlanceTaps = GlanceTaps(
    onTime = this::openAlarms,
    onDate = this::openCalendarAt,
    onEvent = this::openEvent,
    onAlarm = this::openAlarms,
    onEndQuiet = this::endQuiet,
    onExtendQuiet = this::extendQuiet,
)
