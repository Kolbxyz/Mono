package com.wander.mono.core.notifications

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Whether the user granted notification access, which both the dots and the media sessions need.
 * Re-read when the listener connects or disconnects and when the home screen starts (the user may
 * come back from the settings page), never on a timer.
 */
@Singleton
class ListenerAccess @Inject constructor(@ApplicationContext private val context: Context) {
    val component = ComponentName(context, MonoNotificationListener::class.java)

    private val mutableGranted = MutableStateFlow(isGranted())
    val granted: StateFlow<Boolean> = mutableGranted.asStateFlow()

    fun recheck() {
        mutableGranted.value = isGranted()
    }

    fun openSettings(): ActionResult {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                .putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, component.flattenToString())
        } else {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        }
        return try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            ActionResult.Done
        } catch (_: ActivityNotFoundException) {
            ActionResult.Failed(R.string.error_settings_missing)
        }
    }

    private fun isGranted(): Boolean =
        context.packageName in NotificationManagerCompat.getEnabledListenerPackages(context)
}
