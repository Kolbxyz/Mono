package com.wander.mono.ui.home

import androidx.lifecycle.ViewModel
import com.wander.mono.core.calendar.CalendarActions
import com.wander.mono.core.calendar.CalendarEvent
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Hands an event's meeting link or place to the app that takes it. */
@HiltViewModel
class CalendarEventViewModel @Inject constructor(
    private val calendar: CalendarActions,
    private val messages: UserMessages,
) : ViewModel() {
    fun join(event: CalendarEvent) {
        messages.report(calendar.join(event))
    }

    fun openMap(event: CalendarEvent) {
        messages.report(calendar.openMap(event))
    }
}
