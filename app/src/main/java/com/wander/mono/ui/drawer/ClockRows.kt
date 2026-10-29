package com.wander.mono.ui.drawer

import android.icu.text.DateFormat
import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.text.format.DateFormat as AndroidDateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.wander.mono.R
import com.wander.mono.ui.components.AppIcons
import java.util.Calendar
import java.util.Locale

/** "Set a 10 min timer": tapping hands it to the clock app. */
@Composable
fun TimerRow(hit: SearchHit.Timer, icons: AppIcons, onSet: () -> Unit, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val length = remember(hit.seconds, locale) { durationText(hit.seconds, locale) }
    SymbolRow(
        symbol = R.drawable.sym_timer,
        title = stringResource(R.string.search_timer, length),
        subtitle = stringResource(R.string.search_timer_kind),
        clickLabel = R.string.action_set,
        icons = icons,
        onClick = onSet,
        modifier = modifier,
    )
}

/** "Set an alarm for 7:30 AM", in the user's 12/24-hour setting: tapping hands it to the clock app. */
@Composable
fun AlarmRow(hit: SearchHit.Alarm, icons: AppIcons, onSet: () -> Unit, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val skeleton = if (AndroidDateFormat.is24HourFormat(LocalContext.current)) "Hm" else "hm"
    val time = remember(hit.time, skeleton, locale) { timeText(hit.time, skeleton, locale) }
    SymbolRow(
        symbol = R.drawable.sym_alarm,
        title = stringResource(R.string.search_alarm, time),
        subtitle = stringResource(R.string.search_alarm_kind),
        clickLabel = R.string.action_set,
        icons = icons,
        onClick = onSet,
        modifier = modifier,
    )
}

/** "1 hr, 30 min" in the locale's short units, leaving out parts that are zero. */
private fun durationText(seconds: Int, locale: Locale): String {
    val parts = listOf(
        Measure(seconds / SECONDS_PER_HOUR, MeasureUnit.HOUR),
        Measure(seconds % SECONDS_PER_HOUR / SECONDS_PER_MINUTE, MeasureUnit.MINUTE),
        Measure(seconds % SECONDS_PER_MINUTE, MeasureUnit.SECOND),
    ).filter { it.number.toInt() != 0 }
    return MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.SHORT).formatMeasures(*parts.toTypedArray())
}

/** ICU picks the locale's order and separators from the [skeleton], as the clock on home does. */
private fun timeText(time: AlarmTime, skeleton: String, locale: Locale): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, time.hour)
        set(Calendar.MINUTE, time.minute)
    }
    return DateFormat.getInstanceForSkeleton(skeleton, locale).format(calendar.time)
}

private const val SECONDS_PER_HOUR = 3600
private const val SECONDS_PER_MINUTE = 60
