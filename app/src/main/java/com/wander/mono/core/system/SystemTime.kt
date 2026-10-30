package com.wander.mono.core.system

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * The clock and next alarm of the glance header, driven by the system's own broadcasts: the
 * per-minute tick, time/zone/date changes (also sent when the 24-hour setting flips) and the
 * next-alarm change. Registered only while collected.
 */
@Singleton
class SystemTime @Inject constructor(@ApplicationContext private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    val now: Flow<Long> = broadcasts(
        Intent.ACTION_TIME_TICK,
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_TIMEZONE_CHANGED,
        Intent.ACTION_DATE_CHANGED,
    ) { System.currentTimeMillis() }

    /** The wall clock set or moved to another zone (not its ticking): times computed ahead are stale. */
    val changed: Flow<Long> = broadcasts(Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED) {
        System.currentTimeMillis()
    }

    /** Trigger time of the next alarm-clock alarm, null when none is set. */
    val nextAlarm: Flow<Long?> = broadcasts(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED) {
        alarmManager.nextAlarmClock?.triggerTime
    }

    private fun <T> broadcasts(vararg actions: String, read: () -> T): Flow<T> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(read())
            }
        }
        val filter = IntentFilter().apply { actions.forEach(::addAction) }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        trySend(read())
        awaitClose { context.unregisterReceiver(receiver) }
    }
}
