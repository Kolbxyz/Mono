package com.wander.mono.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppActions
import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.media.MediaSessions
import com.wander.mono.core.notifications.ListenerAccess
import com.wander.mono.core.notifications.NotificationCounts
import com.wander.mono.core.quiet.QuietMode
import com.wander.mono.core.system.SystemTime
import com.wander.mono.data.LauncherPrefs
import com.wander.mono.data.settings.BadgeSettings
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    catalog: AppCatalog,
    prefs: LauncherPrefs,
    counts: NotificationCounts,
    settings: SettingsStore,
    time: SystemTime,
    quiet: QuietMode,
    private val media: MediaSessions,
    private val listenerAccess: ListenerAccess,
    private val actions: AppActions,
    private val messages: UserMessages,
) : ViewModel() {
    /** Null until the catalog is loaded, so an empty home is never shown by mistake. */
    val rows: StateFlow<List<HomeRow>?> = combine(
        prefs.favorites,
        catalog.apps.filterNotNull(),
        counts.counts,
        media.state,
        settings[BadgeSettings.ShowCounts],
        ::assembleHomeRows,
    )
        .combine(quiet.state, ::quietHomeRows)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    val glance: StateFlow<Glance> = combine(time.now, time.nextAlarm, ::Glance)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            Glance(System.currentTimeMillis(), null),
        )

    fun launch(app: LaunchableApp) {
        messages.report(actions.launch(app.key))
    }

    fun togglePlayPause() = media.togglePlayPause()

    fun skipNext() = media.skipNext()

    fun skipPrevious() = media.skipPrevious()

    fun openListenerSettings() {
        messages.report(listenerAccess.openSettings())
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
