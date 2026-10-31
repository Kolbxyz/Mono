package com.wander.mono.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.notifications.QuietDigest
import com.wander.mono.core.system.SystemTime
import com.wander.mono.data.LaunchHours
import com.wander.mono.data.LauncherPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Home reading the moment, all on the device: the favourite likely wanted at this hour (worked out
 * again only when the hour turns, on the clock's own tick), the launches that teach it, and the quiet
 * digest once seen.
 */
@HiltViewModel
class HomeContextViewModel @Inject constructor(
    private val launches: LaunchHours,
    private val catalog: AppCatalog,
    private val prefs: LauncherPrefs,
    private val digest: QuietDigest,
    time: SystemTime,
) : ViewModel() {
    val likely: StateFlow<AppKey?> = combine(
        launches.counts,
        prefs.favorites,
        time.now.map(::hourOf).distinctUntilChanged(),
    ) { counts, favorites, hour -> likelyFavorite(counts, hour, favorites) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    /** A favourite opened from home: one more launch in this hour. */
    fun launched(key: AppKey) {
        // Not loaded yet, it would forget every app: count nothing rather than wipe the habits.
        val installed = catalog.apps.value?.map { it.key }?.toSet() ?: return
        viewModelScope.launch { launches.record(key, hourOf(System.currentTimeMillis()), installed) }
    }

    fun digestSeen() = digest.clear()

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
