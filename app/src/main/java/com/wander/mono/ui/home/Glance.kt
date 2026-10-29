package com.wander.mono.ui.home

import com.wander.mono.core.calendar.CalendarAgenda
import com.wander.mono.core.calendar.CalendarEvent
import com.wander.mono.core.calendar.headerEvent

/** What the glance header shows: the current time and the next alarm (epoch millis), and the next event. */
data class Glance(val now: Long, val nextAlarm: Long?, val nextEvent: CalendarEvent? = null)

/**
 * The header's next event (spec 4.2): only when home shows no calendar row, so an event is never
 * listed twice. Switching the calendar row off brings the line back. Nothing while [rows] load.
 */
internal fun glanceEvent(agenda: CalendarAgenda?, now: Long, rows: List<HomeRow>?): CalendarEvent? {
    if (agenda == null || rows == null || rows.any { it is CalendarRow }) return null
    return headerEvent(agenda.events, now)
}
