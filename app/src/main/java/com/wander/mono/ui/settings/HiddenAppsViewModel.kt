package com.wander.mono.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.security.HiddenAppsLock
import com.wander.mono.data.LauncherPrefs
import com.wander.mono.data.settings.HiddenAppSettings
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The hidden apps list in settings, behind the screen lock when the user asked for it. */
@HiltViewModel
class HiddenAppsViewModel @Inject constructor(
    catalog: AppCatalog,
    private val prefs: LauncherPrefs,
    private val settings: SettingsStore,
    private val lock: HiddenAppsLock,
    private val messages: UserMessages,
) : ViewModel() {
    /** Installed hidden apps, A to Z; null until loaded. */
    val apps: StateFlow<List<LaunchableApp>?> = combine(catalog.apps.filterNotNull(), prefs.hidden) { all, hidden ->
        all.filter { it.key in hidden }.sortedBy { it.label.lowercase() }
    }.stateIn(viewModelScope, STARTED, null)

    val locks: StateFlow<Boolean?> = lock.locks.stateIn(viewModelScope, STARTED, null)

    val revealed: StateFlow<Boolean?> = lock.revealed.stateIn(viewModelScope, STARTED, null)

    fun unhide(key: AppKey) {
        viewModelScope.launch { prefs.setHidden(key, false) }
    }

    fun setLocks(on: Boolean) {
        viewModelScope.launch { settings.set(HiddenAppSettings.Lock, on) }
    }

    fun unlock() = lock.unlock()

    fun reportNoScreenLock() {
        messages.report(ActionResult.Failed(R.string.error_no_screen_lock))
    }

    private companion object {
        val STARTED = SharingStarted.WhileSubscribed(5_000L)
    }
}
