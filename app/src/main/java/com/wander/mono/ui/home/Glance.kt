package com.wander.mono.ui.home

import com.wander.mono.core.calendar.CalendarAgenda
import com.wander.mono.core.calendar.CalendarEvent
import com.wander.mono.core.calendar.headerEvent
import com.wander.mono.core.system.BatteryState
import com.wander.mono.core.system.isWorthShowing
import com.wander.mono.data.settings.ClockLook
import java.util.concurrent.TimeUnit

/**
 * What the glance header shows: the current time, the next alarm (epoch millis, only within a day),
 * the next event, the battery when worth a chip, and whether quiet mode is on (with its end, or 0).
 * [look] is the user's clock style and which lines they want.
 */
data class Glance(
    val now: Long,
    val nextAlarm: Long?,
    val nextEvent: CalendarEvent? = null,
    val battery: BatteryState? = null,
    val quiet: QuietGlance? = null,
    val look: ClockLook = ClockLook(),
)

/** Quiet mode is on; [until] is when it ends by itself, 0 when it does not. */
data class QuietGlance(val until: Long)

/**
 * The header's event (spec 4.2): only when home shows no calendar row, so an event is never
 * listed twice. Switching the calendar row off brings the line back. Nothing while [rows] load.
 * The next event when it starts soon ([IMMINENT_MS]); otherwise the one under way, if any, so the
 * header says how long it runs; otherwise the next one within the header's window.
 */
internal fun glanceEvent(agenda: CalendarAgenda?, now: Long, rows: List<HomeRow>?): CalendarEvent? {
    if (agenda == null || rows == null || rows.any { it is CalendarRow }) return null
    val next = headerEvent(agenda.events, now)
    if (next != null && next.begin - now <= IMMINENT_MS) return next
    return currentEvent(agenda.events, now) ?: next
}

/** The timed event under way that ends first: the one the user is most likely in. */
internal fun currentEvent(events: List<CalendarEvent>, now: Long): CalendarEvent? = events
    .filter { !it.allDay && !it.declined && it.begin <= now && it.end > now }
    .minByOrNull { it.end }

/** The next alarm only when it rings within the next 24 hours (spec 4.2); a far one is noise. */
internal fun alarmWithinDay(alarm: Long?, now: Long): Long? =
    alarm?.takeIf { it >= now && it - now <= ALARM_HORIZON_MS }

/**
 * How long until [alarm] rings, in whole minutes rounded up, when it rings within
 * [COUNTDOWN_HORIZON_MS]: the night's sleep, or a nap. Null for a later or missing alarm.
 */
internal fun alarmCountdownMinutes(alarm: Long?, now: Long): Long? {
    val left = alarm?.minus(now)?.takeIf { it in 0..COUNTDOWN_HORIZON_MS } ?: return null
    return (left + MINUTE_MS - 1) / MINUTE_MS
}

/** The battery only when low or charging. */
internal fun glanceBattery(state: BatteryState?): BatteryState? = state?.takeIf { it.isWorthShowing() }

private val ALARM_HORIZON_MS = TimeUnit.DAYS.toMillis(1)

/** Within this, the next event outranks the one under way: it is time to go. */
private val IMMINENT_MS = TimeUnit.MINUTES.toMillis(15)

/** Within this the alarm also says how long is left; further out a countdown is noise. */
private val COUNTDOWN_HORIZON_MS = TimeUnit.HOURS.toMillis(12)

private val MINUTE_MS = TimeUnit.MINUTES.toMillis(1)
