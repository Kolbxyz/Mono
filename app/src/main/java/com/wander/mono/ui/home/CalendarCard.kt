package com.wander.mono.ui.home

import android.text.format.DateFormat as AndroidDateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.calendar.CalendarEvent
import com.wander.mono.ui.theme.isLargeFontScale
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

/**
 * The next few events, one calm line each: when, then what. Tapping a line opens that event; the
 * next one may also offer Join (a meeting link) and Map (its location) as tonal chips. Above
 * 1.5× font scale the time goes above the title, which then keeps the full width.
 */
@Composable
fun CalendarCard(row: CalendarRow, onOpen: (CalendarEvent) -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = HomeCardMargin),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        // Lines pad themselves, so their touch targets reach the card's edges and the text sits at its inset.
        Column(Modifier.padding(vertical = HomeCardInset - LINE_PADDING)) {
            row.events.forEachIndexed { index, event ->
                EventLine(event, row.at, next = index == 0, onOpen)
                // Only the next event offers Join and Map, so the card stays calm.
                if (index == 0) CalendarEventActions(event, row.canOpenMaps)
            }
        }
    }
}

/** The [next] event leads: its time in the accent and its title larger; the rest step back. */
@Composable
private fun EventLine(event: CalendarEvent, at: Long, next: Boolean, onOpen: (CalendarEvent) -> Unit) {
    val title = event.title.ifBlank { stringResource(R.string.calendar_untitled) }
    val line = Modifier
        .fillMaxWidth()
        .heightIn(min = MIN_LINE_HEIGHT)
        .clickable(onClickLabel = stringResource(R.string.action_open_event)) { onOpen(event) }
        .padding(horizontal = HomeCardInset, vertical = LINE_PADDING)
    if (isLargeFontScale()) {
        Column(line) {
            WhenText(event, at, next)
            TitleText(title, next)
        }
    } else {
        Row(line, horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            WhenText(event, at, next, Modifier.widthIn(min = WHEN_WIDTH))
            TitleText(title, next)
        }
    }
}

@Composable
private fun WhenText(event: CalendarEvent, at: Long, next: Boolean, modifier: Modifier = Modifier) {
    Text(
        whenLabel(event, at),
        style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
        color = if (next) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Composable
private fun TitleText(title: String, next: Boolean) {
    Text(
        title,
        style = if (next) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
        color = if (next) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
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
private val LINE_PADDING = 8.dp
private val WHEN_WIDTH = 72.dp

/** Each line is its own touch target (spec 9). */
private val MIN_LINE_HEIGHT = 48.dp
