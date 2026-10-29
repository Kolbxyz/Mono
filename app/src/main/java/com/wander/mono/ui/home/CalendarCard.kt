package com.wander.mono.ui.home

import android.text.format.DateFormat as AndroidDateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.calendar.CalendarEvent
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

/** The next few events, one calm line each: when, then what. Tapping a line opens that event. */
@Composable
fun CalendarCard(row: CalendarRow, onOpen: (CalendarEvent) -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(vertical = 8.dp)) {
            row.events.forEach { event -> EventLine(event, row.at, onOpen) }
        }
    }
}

@Composable
private fun EventLine(event: CalendarEvent, at: Long, onOpen: (CalendarEvent) -> Unit) {
    val title = event.title.ifBlank { stringResource(R.string.calendar_untitled) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = stringResource(R.string.action_open_event)) { onOpen(event) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            whenLabel(event, at),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            modifier = Modifier.widthIn(min = WHEN_WIDTH),
        )
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** "All day", "Now", "14:00", or with the weekday ("Tue 09:00") when it is not today. */
@Composable
private fun whenLabel(event: CalendarEvent, at: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    val timeSkeleton = if (AndroidDateFormat.is24HourFormat(LocalContext.current)) "Hm" else "hm"
    return when {
        event.allDay -> stringResource(R.string.calendar_all_day)
        event.begin <= at -> stringResource(R.string.calendar_now)
        else -> remember(event.begin, at, timeSkeleton, locale) { timeLabel(event.begin, at, timeSkeleton, locale) }
    }
}

private fun timeLabel(begin: Long, at: Long, timeSkeleton: String, locale: Locale): String {
    val zone = ZoneId.systemDefault()
    val sameDay = Instant.ofEpochMilli(begin).atZone(zone).toLocalDate() ==
        Instant.ofEpochMilli(at).atZone(zone).toLocalDate()
    return format(begin, if (sameDay) timeSkeleton else WEEKDAY_SKELETON + timeSkeleton, locale)
}

private const val WEEKDAY_SKELETON = "EEE"
private val WHEN_WIDTH = 72.dp
