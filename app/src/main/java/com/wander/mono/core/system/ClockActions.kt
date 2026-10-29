package com.wander.mono.core.system

import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import androidx.annotation.StringRes
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hands time to the clock and calendar apps: the glance header's lines (spec 4.2) open the alarm
 * list or the calendar at a day, and search sets timers and alarms. Setting opens the clock app's
 * own screen (no `EXTRA_SKIP_UI`), so the user sees what was set and can change or cancel it.
 */
@Singleton
class ClockActions @Inject constructor(@ApplicationContext private val context: Context) {
    /** The clock app's alarm list, for both the time and the next-alarm line. */
    fun openAlarms(): ActionResult = start(Intent(AlarmClock.ACTION_SHOW_ALARMS), R.string.error_clock_missing)

    /** The calendar app opened on the day of [millis]. */
    fun openCalendarAt(millis: Long): ActionResult {
        val day = CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
        ContentUris.appendId(day, millis)
        return start(Intent(Intent.ACTION_VIEW).setData(day.build()), R.string.error_calendar_app_missing)
    }

    /** Resolves an intent, so not on the main thread. */
    fun canSetTimer(): Boolean = handles(AlarmClock.ACTION_SET_TIMER)

    /** Resolves an intent, so not on the main thread. */
    fun canSetAlarm(): Boolean = handles(AlarmClock.ACTION_SET_ALARM)

    fun setTimer(seconds: Int): ActionResult = start(
        Intent(AlarmClock.ACTION_SET_TIMER)
            .putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            .putExtra(AlarmClock.EXTRA_SKIP_UI, false),
        R.string.error_no_clock_app,
    )

    fun setAlarm(hour: Int, minute: Int): ActionResult = start(
        Intent(AlarmClock.ACTION_SET_ALARM)
            .putExtra(AlarmClock.EXTRA_HOUR, hour)
            .putExtra(AlarmClock.EXTRA_MINUTES, minute)
            .putExtra(AlarmClock.EXTRA_SKIP_UI, false),
        R.string.error_no_clock_app,
    )

    private fun handles(action: String): Boolean =
        context.packageManager.resolveActivity(Intent(action), PackageManager.MATCH_DEFAULT_ONLY) != null

    private fun start(intent: Intent, @StringRes missing: Int): ActionResult = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        ActionResult.Done
    } catch (_: ActivityNotFoundException) {
        ActionResult.Failed(missing)
    } catch (_: SecurityException) {
        // A clock app may guard its screens with SET_ALARM.
        ActionResult.Failed(R.string.error_not_allowed)
    }
}
