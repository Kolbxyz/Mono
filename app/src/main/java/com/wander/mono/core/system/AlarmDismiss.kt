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
 * Dismisses the next alarm from the glance header's alarm line (long press), through the clock
 * app: `ACTION_DISMISS_ALARM` in "next" search mode. The clock app does the dismissing and shows
 * it, so nothing is cancelled out of the user's sight. Not every clock app takes this action, so
 * it is resolved first and a clock app without it says so instead of failing silently.
 */
@Singleton
class AlarmDismiss @Inject constructor(@ApplicationContext private val context: Context) {
    /** Resolves an intent, so not on the main thread. */
    fun dismissNext(): ActionResult {
        val intent = Intent(AlarmClock.ACTION_DISMISS_ALARM)
            .putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_NEXT)
        if (context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            return ActionResult.Failed(R.string.error_alarm_dismiss_missing)
        }
        return try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            ActionResult.Done
        } catch (_: ActivityNotFoundException) {
            ActionResult.Failed(R.string.error_alarm_dismiss_missing)
        } catch (_: SecurityException) {
            // A clock app may guard its screens with SET_ALARM.
            ActionResult.Failed(R.string.error_not_allowed)
        }
    }
}
