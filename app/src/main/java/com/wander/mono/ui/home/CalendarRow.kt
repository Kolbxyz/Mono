package com.wander.mono.ui.home

import com.wander.mono.core.calendar.CalendarAgenda
import com.wander.mono.core.calendar.CalendarEvent

/** The next few calendar events, read at [at] (the row labels days relative to it). */
data class CalendarRow(val events: List<CalendarEvent>, val at: Long) : HomeRow {
    override val key: String get() = "calendar"
}

/** How many events the row lists. */
internal const val CALENDAR_ROW_EVENTS = 3

/**
 * Adds the calendar row to the assembled [rows]: right under the main-profile favourite that is the
 * default calendar app, or first (just under the header) when that app is not a favourite. No
 * events (or the feature off) means no row: a live row only shows real data.
 */
internal fun withCalendarRow(rows: List<HomeRow>, agenda: CalendarAgenda?): List<HomeRow> {
    if (agenda == null || agenda.events.isEmpty()) return rows
    val ownerIndex = rows.indexOfFirst {
        it is HomeRow.App && it.app.isMainProfile && it.app.key.packageName == agenda.appPackage
    }
    return rows.toMutableList().apply {
        add(ownerIndex + 1, CalendarRow(agenda.events.take(CALENDAR_ROW_EVENTS), agenda.at))
    }
}
