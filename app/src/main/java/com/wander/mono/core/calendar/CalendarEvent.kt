package com.wander.mono.core.calendar

/**
 * One occurrence of a calendar event. [begin] and [end] are epoch millis in local time terms: an
 * all-day event runs from local midnight to local midnight, not from UTC midnight as stored.
 */
data class CalendarEvent(
    val eventId: Long,
    /** Blank when the event has no title; the UI shows its own placeholder text then. */
    val title: String,
    val begin: Long,
    val end: Long,
    val allDay: Boolean,
    /** The user is an attendee and said no. */
    val declined: Boolean,
)

/**
 * What the calendar row and header show: the upcoming [events], when they were read ([at]) and the
 * package of the default calendar app, so the row can sit under it when it is a favourite.
 */
data class CalendarAgenda(val events: List<CalendarEvent>, val at: Long, val appPackage: String?)
