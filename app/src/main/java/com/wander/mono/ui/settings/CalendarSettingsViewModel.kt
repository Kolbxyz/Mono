package com.wander.mono.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import com.wander.mono.core.calendar.CalendarAccess
import com.wander.mono.data.settings.CalendarSettings
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * The calendar switch: it reads as on only when the setting is on and the permission granted, and
 * turning it on asks for the permission first. A denied permission leaves it off.
 */
@HiltViewModel
class CalendarSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val access: CalendarAccess,
    private val store: SettingsStore,
    private val messages: UserMessages,
) : ViewModel() {
    val shown: Flow<Boolean> = combine(store[CalendarSettings.Enabled], access.granted) { on, granted ->
        on && granted
    }

    val granted: StateFlow<Boolean> = access.granted

    fun setShown(shown: Boolean) {
        viewModelScope.launch { store.set(CalendarSettings.Enabled, shown) }
    }

    /** The permission prompt answered: turn the feature on only if it was granted. */
    fun onPermissionResult(granted: Boolean) {
        access.recheck()
        if (granted) setShown(true)
    }

    /** Android no longer shows the prompt once denied twice; App info is the way left. */
    fun openAppInfo() {
        val uri = Uri.fromParts("package", context.packageName, null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val result = try {
            context.startActivity(intent)
            ActionResult.Done
        } catch (_: ActivityNotFoundException) {
            ActionResult.Failed(R.string.error_settings_missing)
        }
        messages.report(result)
    }
}
