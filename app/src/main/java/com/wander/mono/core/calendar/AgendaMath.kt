package com.wander.mono.core.calendar

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

/** How far ahead the calendar row looks. */
internal val AGENDA_WINDOW_MS = TimeUnit.HOURS.toMillis(24)

/** How far ahead the glance header looks for the next event. */
internal val HEADER_WINDOW_MS = TimeUnit.HOURS.toMillis(12)

/** One day back covers all-day events stored at UTC midnight in zones west of UTC. */
internal val QUERY_BEFORE_MS = TimeUnit.DAYS.toMillis(1)

/** Two days ahead covers the row's window plus the events that will enter it before the next read. */
internal val QUERY_AFTER_MS = TimeUnit.DAYS.toMillis(2)

/** The calendar provider stores all-day events at UTC midnight; this is the same date's local midnight. */
internal fun allDayToLocal(utcMillis: Long, zone: ZoneId): Long =
    Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
        .atStartOfDay(zone).toInstant().toEpochMilli()

/** Events not over yet and starting within the window, declined ones dropped, in start order. */
internal fun upcomingEvents(events: List<CalendarEvent>, now: Long): List<CalendarEvent> = events
    .filter { !it.declined && it.end > now && it.begin < now + AGENDA_WINDOW_MS }
    .sortedWith(compareBy({ it.begin }, { !it.allDay }, { it.title }))

/** The next timed event not started yet and starting within the header's window, if any. */
internal fun headerEvent(events: List<CalendarEvent>, now: Long): CalendarEvent? = events
    .filter { !it.allDay && !it.declined && it.begin >= now && it.begin - now <= HEADER_WINDOW_MS }
    .minByOrNull { it.begin }

/**
 * When the upcoming list next changes without the provider telling us: an event starts, ends or
 * enters the window, local midnight passes (the row's day labels change), or the [queriedAt] read
 * no longer covers the window. Read again then, rather than every minute.
 */
internal fun nextBoundary(events: List<CalendarEvent>, queriedAt: Long, zone: ZoneId): Long {
    val midnight = Instant.ofEpochMilli(queriedAt).atZone(zone).toLocalDate().plusDays(1)
        .atStartOfDay(zone).toInstant().toEpochMilli()
    val candidates = events.flatMap { listOf(it.begin, it.end, it.begin - AGENDA_WINDOW_MS) }
        .filter { it > queriedAt }
    val coverageEnd = queriedAt + QUERY_AFTER_MS - AGENDA_WINDOW_MS
    return (candidates + midnight + coverageEnd).min()
}
