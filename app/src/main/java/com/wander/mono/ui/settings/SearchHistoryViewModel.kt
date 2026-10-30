package com.wander.mono.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.data.SearchHistory
import com.wander.mono.data.settings.SearchSettings
import com.wander.mono.data.settings.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Remembering recent searches: switching it off forgets what was remembered, so off always means
 * nothing is kept; Clear forgets it while leaving the switch on.
 */
@HiltViewModel
class SearchHistoryViewModel @Inject constructor(
    private val store: SettingsStore,
    private val history: SearchHistory,
) : ViewModel() {
    /** Whether anything is remembered, for Clear to show only when there is something to clear. */
    val hasHistory: Flow<Boolean> = history.recent.map { it.isNotEmpty() }

    fun setRemembering(on: Boolean) {
        viewModelScope.launch {
            store.set(SearchSettings.RememberRecent, on)
            if (!on) history.clear()
        }
    }

    fun clear() {
        viewModelScope.launch { history.clear() }
    }
}
