package com.wander.mono.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.data.LauncherPrefs
import com.wander.mono.data.Routine
import com.wander.mono.data.Routines
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The routines settings section: the user's routines and the favourites they can put first. */
@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val store: Routines,
    prefs: LauncherPrefs,
    catalog: AppCatalog,
) : ViewModel() {
    /** Null until read, so the section never shows an empty list by mistake. */
    val routines: StateFlow<List<Routine>?> =
        store.routines.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    /** The installed favourites, in home's order. */
    val favorites: StateFlow<List<LaunchableApp>> = combine(
        prefs.favorites,
        catalog.apps.filterNotNull(),
    ) { keys, apps ->
        val installed = apps.associateBy { it.key }
        keys.mapNotNull(installed::get)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    /** A new routine over a usual morning, with no apps yet: the user picks them. */
    fun add() = launch { store.add(Routine(DEFAULT_START, DEFAULT_END, emptySet())) }

    fun update(index: Int, routine: Routine) = launch { store.update(index, routine) }

    fun remove(index: Int) = launch { store.remove(index) }

    private fun launch(change: suspend () -> Unit) {
        viewModelScope.launch { change() }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        const val DEFAULT_START = 7 * Routine.MINUTES_PER_HOUR
        const val DEFAULT_END = 9 * Routine.MINUTES_PER_HOUR
    }
}
