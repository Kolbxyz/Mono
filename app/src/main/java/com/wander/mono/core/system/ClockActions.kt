package com.wander.mono.core.system

import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.provider.CalendarContract
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Where the glance header's lines lead (spec 4.2): the clock app's alarms, and the calendar at a day. */
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

    private fun start(intent: Intent, missing: Int): ActionResult = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        ActionResult.Done
    } catch (_: ActivityNotFoundException) {
        ActionResult.Failed(missing)
    } catch (_: SecurityException) {
        // A clock app may guard its alarm screens with SET_ALARM.
        ActionResult.Failed(R.string.error_not_allowed)
    }
}
