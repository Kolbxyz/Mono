package com.wander.mono.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import com.wander.mono.core.apps.AppActions
import com.wander.mono.core.calendar.CalendarAccess
import com.wander.mono.core.contacts.ContactSearch
import com.wander.mono.core.notifications.ListenerAccess
import com.wander.mono.core.quiet.QuietZenRule
import com.wander.mono.core.system.GestureActions
import com.wander.mono.core.system.HomeRole
import com.wander.mono.core.usage.AppUsage
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * "What Mono can see": every access Mono may hold and whether it does. Notification access and the
 * gesture service report changes as they happen; the rest send no event, so they are read again
 * when settings come back to the screen ([recheck]), never on a timer.
 */
@HiltViewModel
class PrivacyViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val listener: ListenerAccess,
    private val usage: AppUsage,
    private val calendar: CalendarAccess,
    private val contacts: ContactSearch,
    private val quietRule: QuietZenRule,
    private val gestures: GestureActions,
    private val homeRole: HomeRole,
    private val actions: AppActions,
    private val messages: UserMessages,
) : ViewModel() {
    private val eventless = MutableStateFlow(readEventless())

    val accesses: StateFlow<List<AccessState>> =
        combine(eventless, listener.granted, gestures.isAvailable) { read, notifications, gestureService ->
            accessesOn(Build.VERSION.SDK_INT).map { access ->
                val granted = when (access) {
                    PrivacyAccess.Notifications -> notifications
                    PrivacyAccess.Gestures -> gestureService
                    else -> access in read
                }
                AccessState(access, granted)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    fun recheck() {
        listener.recheck()
        calendar.recheck()
        eventless.value = readEventless()
    }

    /** Android's own page for [access], where the user grants or takes it back. */
    fun open(access: PrivacyAccess) {
        val result = when (access) {
            PrivacyAccess.Notifications -> listener.openSettings()
            PrivacyAccess.Usage -> usage.openSettings()
            // Runtime permissions are granted and revoked on the app's own info page.
            PrivacyAccess.Calendar, PrivacyAccess.Contacts -> actions.openOwnAppInfo()
            PrivacyAccess.DoNotDisturb -> quietRule.openAccessSettings()
            PrivacyAccess.Gestures -> gestures.openAccessSettings()
            // Android lets only the default home app see the private space.
            PrivacyAccess.HiddenProfiles -> openHomeSettings()
        }
        messages.report(result)
    }

    /** The accesses granted among those that send no event when they change. */
    private fun readEventless(): Set<PrivacyAccess> = buildSet {
        if (usage.isGranted()) add(PrivacyAccess.Usage)
        if (calendar.granted.value) add(PrivacyAccess.Calendar)
        if (contacts.hasPermission()) add(PrivacyAccess.Contacts)
        if (quietRule.isGranted()) add(PrivacyAccess.DoNotDisturb)
        if (homeRole.isHeld()) add(PrivacyAccess.HiddenProfiles)
    }

    private fun openHomeSettings(): ActionResult = try {
        context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        ActionResult.Done
    } catch (_: ActivityNotFoundException) {
        ActionResult.Failed(R.string.error_settings_missing)
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
