package com.wander.mono.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.wander.mono.R
import com.wander.mono.core.calendar.CalendarEvent
import com.wander.mono.ui.theme.secondaryTextColor
import java.util.concurrent.TimeUnit

/** "Standup · 14:00", or "Standup · in 20 min" within the hour; recomposed on each minute tick. */
@Composable
fun NextEventLine(event: CalendarEvent, now: Long, timeSkeleton: String, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val title = event.title.ifBlank { stringResource(R.string.calendar_untitled) }
    val minutes = minutesUntil(event.begin, now)
    val time = when {
        minutes < 1 -> stringResource(R.string.home_event_now)
        minutes < MINUTES_PER_HOUR -> pluralStringResource(R.plurals.home_event_in_minutes, minutes, minutes)
        else -> remember(event.begin, timeSkeleton, locale) { format(event.begin, timeSkeleton, locale) }
    }
    Text(
        stringResource(R.string.home_next_event, title, time),
        style = MaterialTheme.typography.titleMedium.onWallpaper(),
        color = secondaryTextColor(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

/** Whole minutes left, rounded up so an event at 14:00 reads "in 1 min" at 13:59:30. */
internal fun minutesUntil(begin: Long, now: Long): Int {
    val left = begin - now
    if (left <= 0) return 0
    return ((left + MINUTE_MS - 1) / MINUTE_MS).toInt()
}

private val MINUTE_MS = TimeUnit.MINUTES.toMillis(1)
private const val MINUTES_PER_HOUR = 60
