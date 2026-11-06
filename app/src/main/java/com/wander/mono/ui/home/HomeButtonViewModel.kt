package com.wander.mono.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.data.HomeButtonApp
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * What home's round button opens: the chosen app while it is installed and in a profile that shows
 * it, else search. The choice itself is kept while its app is away (a paused profile, an update), so
 * the button takes it back once the app is listed again.
 */
@HiltViewModel
class HomeButtonViewModel @Inject constructor(
    private val choice: HomeButtonApp,
    catalog: AppCatalog,
) : ViewModel() {
    /** The app the button opens now; null means search. */
    val app: StateFlow<LaunchableApp?> = combine(choice.stored, catalog.apps) { key, apps ->
        key?.let { chosen -> apps?.firstOrNull { it.key == chosen } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    /** An app is chosen, listed or not: the pick screen offers to go back to search. */
    val chosen: StateFlow<Boolean> = choice.stored
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    fun choose(app: AppKey) {
        viewModelScope.launch { choice.set(app) }
    }

    fun useSearch() {
        viewModelScope.launch { choice.clear() }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
