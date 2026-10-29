package com.wander.mono.ui.home

import android.icu.text.DateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.wander.mono.R
import com.wander.mono.ui.theme.CLOCK_MAX_FONT_SCALE
import com.wander.mono.ui.theme.capFontScale
import com.wander.mono.ui.theme.clockStyle
import com.wander.mono.ui.theme.secondaryTextColor
import java.text.CharacterIterator
import java.util.Date
import java.util.Locale
import android.text.format.DateFormat as AndroidDateFormat

/**
 * Big clock, date, next alarm and next calendar event, formatted for the user's locale and 12/24-hour
 * setting. [compact] (a phone in landscape) trades the airy spacing and display size for list room.
 */
@Composable
fun GlanceHeader(glance: Glance, modifier: Modifier = Modifier, compact: Boolean = false) {
    val locale = LocalConfiguration.current.locales[0]
    // Re-read on every minute tick; flipping the setting also sends ACTION_TIME_CHANGED.
    val is24Hour = AndroidDateFormat.is24HourFormat(LocalContext.current)
    val timeSkeleton = if (is24Hour) "Hm" else "hm"
    val time = remember(glance.now, timeSkeleton, locale) { clockText(glance.now, timeSkeleton, locale) }
    val date = remember(glance.now, locale) { format(glance.now, DATE_SKELETON, locale) }
    val alarm = glance.nextAlarm?.let { next ->
        remember(next, timeSkeleton, locale) { format(next, ALARM_DAY_SKELETON + timeSkeleton, locale) }
    }

    Column(modifier.padding(top = if (compact) 8.dp else 48.dp, bottom = if (compact) 16.dp else 32.dp)) {
        val typography = MaterialTheme.typography
        Text(
            time,
            style = clockStyle(compact).capFontScale(CLOCK_MAX_FONT_SCALE).onWallpaper(),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(date, style = typography.titleMedium.onWallpaper(), color = secondaryTextColor())
        glance.nextEvent?.let { NextEventLine(it, glance.now, timeSkeleton) }
        if (alarm != null) {
            Text(
                stringResource(R.string.home_next_alarm, alarm),
                style = MaterialTheme.typography.titleMedium.onWallpaper(),
                color = secondaryTextColor(),
            )
        }
    }
}

/**
 * The time with its AM/PM marker, where the locale has one, set small: the digits carry the clock.
 * ICU reports where the marker sits, wherever the locale puts it.
 */
private fun clockText(millis: Long, skeleton: String, locale: Locale): AnnotatedString {
    val iterator = DateFormat.getInstanceForSkeleton(skeleton, locale).formatToCharacterIterator(Date(millis))
    val text = buildString {
        var c = iterator.first()
        while (c != CharacterIterator.DONE) {
            append(c)
            c = iterator.next()
        }
    }
    return buildAnnotatedString {
        append(text)
        var index = 0
        while (index < text.length) {
            iterator.setIndex(index)
            val end = iterator.runLimit
            if (iterator.attributes.containsKey(DateFormat.Field.AM_PM)) {
                addStyle(SpanStyle(fontSize = MARKER_SIZE), index, end)
            }
            index = end
        }
    }
}

/** ICU picks the right order and separators for the locale from a skeleton. */
internal fun format(millis: Long, skeleton: String, locale: Locale): String =
    DateFormat.getInstanceForSkeleton(skeleton, locale).format(Date(millis))

/** The AM/PM marker, relative to the clock digits. */
private val MARKER_SIZE = 0.4.em

private const val DATE_SKELETON = "EEEEMMMMd"
private const val ALARM_DAY_SKELETON = "EEE"
