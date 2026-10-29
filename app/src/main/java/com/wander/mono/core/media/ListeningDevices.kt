package com.wander.mono.core.media

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Whether something made for listening (headphones, a headset, Bluetooth audio, hearing aids) is
 * connected. Told by the system as devices come and go; registering reports the ones already there.
 */
@Singleton
class ListeningDevices @Inject constructor(@ApplicationContext context: Context) {
    private val audio = context.getSystemService(AudioManager::class.java)

    val connected: Flow<Boolean> = callbackFlow {
        val callback = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(added: Array<out AudioDeviceInfo>) {
                trySend(anyConnected())
            }

            override fun onAudioDevicesRemoved(removed: Array<out AudioDeviceInfo>) {
                trySend(anyConnected())
            }
        }
        audio.registerAudioDeviceCallback(callback, Handler(Looper.getMainLooper()))
        awaitClose { audio.unregisterAudioDeviceCallback(callback) }
    }.distinctUntilChanged()

    private fun anyConnected(): Boolean =
        audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any { it.type in LISTENING_TYPES }

    private companion object {
        /**
         * Plain ints compared with what the system reports: a type newer than the running Android
         * is never reported there, so its constant is harmless on older versions.
         */
        @SuppressLint("InlinedApi")
        val LISTENING_TYPES = setOf(
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_HEARING_AID,
            AudioDeviceInfo.TYPE_BLE_HEADSET,
            AudioDeviceInfo.TYPE_BLE_BROADCAST,
        )
    }
}
