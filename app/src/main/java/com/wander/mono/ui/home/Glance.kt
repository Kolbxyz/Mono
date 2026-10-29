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
 * The header's next event (spec 4.2): only when home shows no calendar row, so an event is never
 * listed twice. Switching the calendar row off brings the line back. Nothing while [rows] load.
 */
internal fun glanceEvent(agenda: CalendarAgenda?, now: Long, rows: List<HomeRow>?): CalendarEvent? {
    if (agenda == null || rows == null || rows.any { it is CalendarRow }) return null
    return headerEvent(agenda.events, now)
}

/** The next alarm only when it rings within the next 24 hours (spec 4.2); a far one is noise. */
internal fun alarmWithinDay(alarm: Long?, now: Long): Long? =
    alarm?.takeIf { it >= now && it - now <= ALARM_HORIZON_MS }

/** The battery only when low or charging. */
internal fun glanceBattery(state: BatteryState?): BatteryState? = state?.takeIf { it.isWorthShowing() }

private val ALARM_HORIZON_MS = TimeUnit.DAYS.toMillis(1)
