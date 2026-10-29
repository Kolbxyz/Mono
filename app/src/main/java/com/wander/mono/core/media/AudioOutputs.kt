package com.wander.mono.core.media

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRouter
import android.media.session.MediaController
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Where the playing media is heard. */
sealed interface AudioOutput {
    /** This device's own route; [name] is the system's label (phone speaker, headphones, a Bluetooth device). */
    data class Local(val name: String) : AudioOutput

    /** The session plays on another device (a cast), whose name the platform does not expose. */
    data object Remote : AudioOutput
}

/**
 * The current audio output and the system's output switcher. The switcher is SystemUI's media
 * output dialog, opened by broadcast for the playing app; when this build of SystemUI has no such
 * receiver there is nothing to open, so no output is reported and the chip stays hidden.
 */
@Singleton
class AudioOutputs @Inject constructor(@ApplicationContext private val context: Context) {
    private val router = context.getSystemService(MediaRouter::class.java)

    private val hasSwitcher: Boolean by lazy {
        val intent = switcherIntent()
        val receivers = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryBroadcastReceivers(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.queryBroadcastReceivers(intent, 0)
        }
        receivers.isNotEmpty()
    }

    /** The output [controller] plays on, or null when it could not be switched from here anyway. */
    fun current(controller: MediaController): AudioOutput? = when {
        !hasSwitcher -> null
        controller.playbackInfo.playbackType == MediaController.PlaybackInfo.PLAYBACK_TYPE_REMOTE -> AudioOutput.Remote
        else -> AudioOutput.Local(localRouteName())
    }

    private fun localRouteName() =
        router.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO).getName(context).toString()

    /** Calls [onChange] on the main thread whenever the system audio route changes; returns the unregister. */
    fun watch(onChange: () -> Unit): () -> Unit {
        val callback = object : MediaRouter.SimpleCallback() {
            override fun onRouteSelected(router: MediaRouter, type: Int, info: MediaRouter.RouteInfo) = onChange()
            override fun onRouteUnselected(router: MediaRouter, type: Int, info: MediaRouter.RouteInfo) = onChange()
            override fun onRouteChanged(router: MediaRouter, info: MediaRouter.RouteInfo) = onChange()
        }
        router.addCallback(MediaRouter.ROUTE_TYPE_LIVE_AUDIO, callback)
        return { router.removeCallback(callback) }
    }

    /** Opens the system output switcher for [packageName]'s session. */
    fun openSwitcher(packageName: String) {
        context.sendBroadcast(switcherIntent().putExtra(EXTRA_PACKAGE_NAME, packageName))
    }

    private fun switcherIntent() = Intent(ACTION_MEDIA_OUTPUT).setPackage(SYSTEM_UI)

    private companion object {
        const val SYSTEM_UI = "com.android.systemui"
        const val ACTION_MEDIA_OUTPUT = "com.android.systemui.action.LAUNCH_MEDIA_OUTPUT_DIALOG"
        const val EXTRA_PACKAGE_NAME = "package_name"
    }
}
