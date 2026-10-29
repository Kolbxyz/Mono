package com.wander.mono.core.system

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.AlarmClock
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hands a timer or an alarm to the clock app with `AlarmClock` intents. Its own screen opens on
 * them (no `EXTRA_SKIP_UI`), so the user sees what was set and can change or cancel it there.
 */
@Singleton
class ClockActions @Inject constructor(@ApplicationContext private val context: Context) {
    /** Resolves an intent, so not on the main thread. */
    fun canSetTimer(): Boolean = handles(AlarmClock.ACTION_SET_TIMER)

    /** Resolves an intent, so not on the main thread. */
    fun canSetAlarm(): Boolean = handles(AlarmClock.ACTION_SET_ALARM)

    fun setTimer(seconds: Int): ActionResult = start(
        Intent(AlarmClock.ACTION_SET_TIMER)
            .putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            .putExtra(AlarmClock.EXTRA_SKIP_UI, false),
    )

    fun setAlarm(hour: Int, minute: Int): ActionResult = start(
        Intent(AlarmClock.ACTION_SET_ALARM)
            .putExtra(AlarmClock.EXTRA_HOUR, hour)
            .putExtra(AlarmClock.EXTRA_MINUTES, minute)
            .putExtra(AlarmClock.EXTRA_SKIP_UI, false),
    )

    private fun handles(action: String): Boolean =
        context.packageManager.resolveActivity(Intent(action), PackageManager.MATCH_DEFAULT_ONLY) != null

    private fun start(intent: Intent): ActionResult = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        ActionResult.Done
    } catch (_: ActivityNotFoundException) {
        ActionResult.Failed(R.string.error_no_clock_app)
    }
}
