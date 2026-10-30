package com.wander.mono.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.data.settings.AccessibilitySettings
import com.wander.mono.data.settings.HomeLookSettings
import com.wander.mono.data.settings.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * How home is arranged rather than how it reads: the clock above the list or, [reachable], below
 * it over the dock; the rows' [labelScale] (1 is the system size); and [simple], the large, simple
 * preset, which keeps only the clock and date over the list.
 */
data class HomeShell(val reachable: Boolean = false, val labelScale: Float = 1f, val simple: Boolean = false)

@HiltViewModel
class HomeShellViewModel @Inject constructor(settings: SettingsStore) : ViewModel() {
    val shell: StateFlow<HomeShell> = combine(
        settings[HomeLookSettings.Reachable],
        settings[HomeLookSettings.LabelScale],
        settings[AccessibilitySettings.SimpleMode],
    ) { reachable, scale, simple -> HomeShell(reachable, scale / PERCENT, simple) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeShell())

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        const val PERCENT = 100f
    }
}
