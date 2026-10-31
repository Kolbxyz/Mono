package com.wander.mono.core.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Whether battery saver is on right now. */
fun isPowerSaveMode(context: Context): Boolean =
    context.getSystemService(PowerManager::class.java)?.isPowerSaveMode == true

/**
 * Battery saver, from `ACTION_POWER_SAVE_MODE_CHANGED`: the current state at once, then each flip
 * the system announces. Registered only while collected; never polled.
 */
fun powerSaveMode(context: Context): Flow<Boolean> = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            trySend(isPowerSaveMode(context))
        }
    }
    val filter = IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
    ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    trySend(isPowerSaveMode(context))
    awaitClose { context.unregisterReceiver(receiver) }
}.distinctUntilChanged()
