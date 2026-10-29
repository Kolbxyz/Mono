package com.wander.mono.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.media.MediaSessions
import com.wander.mono.data.LauncherPrefs
import com.wander.mono.data.LiveRowPrefs
import com.wander.mono.data.LiveRowRef
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Edit mode's changes to the home list: order, removal, additions and per-app live rows. */
@HiltViewModel
class EditHomeViewModel @Inject constructor(
    catalog: AppCatalog,
    media: MediaSessions,
    private val prefs: LauncherPrefs,
    private val liveRows: LiveRowPrefs,
) : ViewModel() {
    /** The live row switches under each installed favourite. */
    val toggles: StateFlow<Map<AppKey, List<LiveRowToggle>>> = combine(
        prefs.favorites,
        catalog.apps.filterNotNull(),
        media.state,
        liveRows.hidden,
    ) { favorites, apps, state, hidden ->
        val installed = apps.associateBy { it.key }
        val shown = favorites.mapNotNull(installed::get)
        liveRowToggles(shown, nowPlayingAvailability(state, shown), hidden)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyMap())

    fun reorder(order: List<AppKey>) = launch { prefs.reorderFavorites(order) }

    fun move(key: AppKey, by: Int) = launch { prefs.moveFavorite(key, by) }

    fun remove(key: AppKey) = launch { prefs.removeFavorite(key) }

    fun add(key: AppKey) = launch { prefs.addFavorite(key) }

    fun setLiveRow(row: LiveRowRef, shown: Boolean) = launch { liveRows.setShown(row, shown) }

    private fun launch(change: suspend () -> Unit) {
        viewModelScope.launch { change() }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
