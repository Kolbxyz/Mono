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
import java.util.Date
import java.util.Locale

/** Big clock, date and next alarm, formatted for the user's locale and 12/24-hour setting. */
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
        Text(time, style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(date, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (alarm != null) {
            Text(
                stringResource(R.string.home_next_alarm, alarm),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** ICU picks the right order and separators for the locale from a skeleton. */
private fun format(millis: Long, skeleton: String, locale: Locale): String =
    DateFormat.getInstanceForSkeleton(skeleton, locale).format(Date(millis))

private const val DATE_SKELETON = "EEEEMMMMd"
private const val ALARM_DAY_SKELETON = "EEE"
