package com.wander.mono.ui.home

import android.icu.text.DateFormat
import android.text.format.DateFormat as AndroidDateFormat
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
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.theme.CLOCK_MAX_FONT_SCALE
import com.wander.mono.ui.theme.capFontScale
import com.wander.mono.ui.theme.secondaryTextColor
import java.util.Date
import java.util.Locale

/** Big clock, date, next alarm and next calendar event, formatted for the user's locale and 12/24-hour setting. */
@Composable
fun GlanceHeader(glance: Glance, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    // Re-read on every minute tick; flipping the setting also sends ACTION_TIME_CHANGED.
    val is24Hour = AndroidDateFormat.is24HourFormat(LocalContext.current)
    val timeSkeleton = if (is24Hour) "Hm" else "hm"
    val time = remember(glance.now, timeSkeleton, locale) { format(glance.now, timeSkeleton, locale) }
    val date = remember(glance.now, locale) { format(glance.now, DATE_SKELETON, locale) }
    val alarm = glance.nextAlarm?.let { next ->
        remember(next, timeSkeleton, locale) { format(next, ALARM_DAY_SKELETON + timeSkeleton, locale) }
    }

    Column(modifier.padding(top = 48.dp, bottom = 32.dp)) {
        val typography = MaterialTheme.typography
        Text(
            time,
            style = typography.displayLargeEmphasized.capFontScale(CLOCK_MAX_FONT_SCALE).onWallpaper(),
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

/** ICU picks the right order and separators for the locale from a skeleton. */
internal fun format(millis: Long, skeleton: String, locale: Locale): String =
    DateFormat.getInstanceForSkeleton(skeleton, locale).format(Date(millis))

private const val DATE_SKELETON = "EEEEMMMMd"
private const val ALARM_DAY_SKELETON = "EEE"
