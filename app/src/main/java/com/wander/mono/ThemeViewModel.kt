package com.wander.mono

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.data.settings.ThemeChoice
import com.wander.mono.data.settings.themeChoice
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * The user's theme, kept across activity recreations (locale, font scale, dark mode), so a new
 * activity draws in it from its first frame. Null only until the first read of the process.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(settings: SettingsStore) : ViewModel() {
    val theme: StateFlow<ThemeChoice?> =
        settings.themeChoice().stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
