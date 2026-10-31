package com.wander.mono.ui.home

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.calendar.CalendarEvent
import com.wander.mono.core.system.canOfferAlarmDismiss
import com.wander.mono.data.settings.GlanceLines
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * The glance facts under the date, as one row of pills that wraps at large font sizes: the event
 * ([lines] permitting), the next alarm, quiet mode ([withQuiet]) and the battery. Each pill opens
 * what it names; a long press on the alarm dismisses it ([onDismissAlarm]) when
 * [canOfferAlarmDismiss] says the clock app would. [taps] null (the settings preview) makes every
 * pill plain. Nothing at all when there is nothing to say.
 */
@Composable
internal fun GlancePills(
    glance: Glance,
    lines: GlanceLines,
    withQuiet: Boolean,
    taps: GlanceTaps?,
    onDismissAlarm: (() -> Unit)?,
    timeSkeleton: String,
    centred: Boolean,
    modifier: Modifier = Modifier,
) {
    val event = glance.nextEvent?.takeIf { lines.event }
    val alarm = glance.nextAlarm?.takeIf { lines.alarm }
    val quiet = glance.quiet?.takeIf { withQuiet }
    val power = glance.battery?.takeIf { lines.battery }
    if (listOfNotNull(event, alarm, quiet, power).isEmpty()) return
    val along = if (centred) Alignment.CenterHorizontally else Alignment.Start
    FlowRow(modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, along)) {
        event?.let { EventPill(it, glance.now, timeSkeleton, taps?.onEvent) }
        alarm?.let { AlarmPill(it, glance.now, timeSkeleton, taps?.onAlarm, onDismissAlarm) }
        quiet?.let { QuietChip(it, timeSkeleton, taps?.onEndQuiet, taps?.onExtendQuiet) }
        power?.let { BatteryChip(it) }
    }
}

/** "Standup · in 20 min", "Standup · 14:00" or, under way, "Standup · until 15:00". */
@Composable
private fun EventPill(event: CalendarEvent, now: Long, timeSkeleton: String, onOpen: ((CalendarEvent) -> Unit)?) {
    val locale = LocalConfiguration.current.locales[0]
    val title = event.title.ifBlank { stringResource(R.string.calendar_untitled) }
    val minutes = minutesUntil(event.begin, now)
    val time = when {
        event.begin <= now && now - event.begin < MINUTE_MS -> stringResource(R.string.home_event_now)
        event.begin <= now -> stringResource(
            R.string.home_event_until,
            remember(event.end, timeSkeleton, locale) { format(event.end, timeSkeleton, locale) },
        )
        minutes < MINUTES_PER_HOUR -> pluralStringResource(R.plurals.home_event_in_minutes, minutes, minutes)
        else -> remember(event.begin, timeSkeleton, locale) { format(event.begin, timeSkeleton, locale) }
    }
    val label = stringResource(R.string.home_next_event, title, time)
    GlancePill(
        icon = R.drawable.sym_event,
        label = label,
        description = label,
        onClickLabel = stringResource(R.string.glance_open_event),
        onClick = onOpen?.let { open -> { open(event) } },
    )
}

/**
 * The next alarm's time and, within twelve hours, how long is left ("07:00 · 7h 20m"): at bedtime,
 * the night's sleep at a look. No weekday: within a day, the time alone cannot be mistaken.
 */
@Composable
private fun AlarmPill(
    alarm: Long,
    now: Long,
    timeSkeleton: String,
    onOpen: (() -> Unit)?,
    onDismissAlarm: (() -> Unit)?,
) {
    val locale = LocalConfiguration.current.locales[0]
    val time = remember(alarm, timeSkeleton, locale) { format(alarm, timeSkeleton, locale) }
    val left = alarmCountdownMinutes(alarm, now)
    val short = left?.let { remember(it, locale) { duration(it, locale, MeasureFormat.FormatWidth.NARROW) } }
    val long = left?.let { remember(it, locale) { duration(it, locale, MeasureFormat.FormatWidth.WIDE) } }
    // Only an alarm the clock app would dismiss; otherwise the long press does nothing here.
    val onDismiss = onDismissAlarm?.takeIf { onOpen != null && canOfferAlarmDismiss(alarm, now) }
    GlancePill(
        icon = R.drawable.sym_alarm,
        label = short?.let { stringResource(R.string.glance_alarm_countdown, time, it) } ?: time,
        description = long?.let { stringResource(R.string.glance_alarm_countdown_description, time, it) }
            ?: stringResource(R.string.home_next_alarm, time),
        onClickLabel = stringResource(R.string.glance_open_alarms),
        onClick = onOpen,
        onLongClickLabel = onDismiss?.let { stringResource(R.string.glance_dismiss_alarm) },
        onLongClick = onDismiss,
    )
}

/** [minutes] as hours and minutes in the locale's words, dropping a part that is zero. */
private fun duration(minutes: Long, locale: Locale, width: MeasureFormat.FormatWidth): String {
    val hours = minutes / MINUTES_PER_HOUR
    val rest = minutes % MINUTES_PER_HOUR
    val parts = buildList {
        if (hours > 0) add(Measure(hours, MeasureUnit.HOUR))
        if (rest > 0 || hours == 0L) add(Measure(rest, MeasureUnit.MINUTE))
    }
    return MeasureFormat.getInstance(locale, width).formatMeasures(*parts.toTypedArray())
}

/** Whole minutes left, rounded up so an event at 14:00 reads "in 1 min" at 13:59:30. */
internal fun minutesUntil(begin: Long, now: Long): Int {
    val left = begin - now
    if (left <= 0) return 0
    return ((left + MINUTE_MS - 1) / MINUTE_MS).toInt()
}

private val MINUTE_MS = TimeUnit.MINUTES.toMillis(1)
private const val MINUTES_PER_HOUR = 60
