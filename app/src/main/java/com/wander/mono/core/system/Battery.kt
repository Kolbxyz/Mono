package com.wander.mono.core.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/** The charge in percent, whether a charger is feeding it and whether battery [saver] is on. */
data class BatteryState(val percent: Int, val charging: Boolean, val saver: Boolean = false)

/** The header's battery chip stays quiet unless the battery is low, charging or saving (spec 4.2). */
fun BatteryState.isWorthShowing(): Boolean = charging || saver || percent <= LOW_BATTERY_PERCENT

const val LOW_BATTERY_PERCENT = 15

/**
 * [BatteryState] from `ACTION_BATTERY_CHANGED`'s extras; null when the intent carries no level
 * (a device without a battery reports none).
 */
fun batteryState(level: Int, scale: Int, status: Int, plugged: Int): BatteryState? {
    if (level < 0 || scale <= 0) return null
    val charging = plugged != 0 &&
        (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL)
    return BatteryState(percent = level * PERCENT / scale, charging = charging)
}

private const val PERCENT = 100

/**
 * The battery, from the sticky `ACTION_BATTERY_CHANGED` broadcast: the registration hands back the
 * last state at once, and the system sends a new one on each change. Battery saver comes from its own
 * broadcast ([powerSaveMode]). Both are registered only while collected.
 */
@Singleton
class Battery @Inject constructor(@ApplicationContext private val context: Context) {
    val state: Flow<BatteryState?> = combine(charge(), powerSaveMode(context)) { charge, saver ->
        charge?.copy(saver = saver)
    }.distinctUntilChanged()

    private fun charge(): Flow<BatteryState?> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(read(intent))
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val sticky = ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        trySend(sticky?.let(::read))
        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged()

    private fun read(intent: Intent): BatteryState? = batteryState(
        level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
        scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1),
        status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN),
        plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0),
    )
}
