package com.wander.mono.ui.quiet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.quiet.QuietMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Quiet mode for home's options and the drawer's gate. */
@HiltViewModel
class QuietViewModel @Inject constructor(private val quiet: QuietMode) : ViewModel() {
    val enabled: StateFlow<Boolean> = quiet.enabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    fun setEnabled(on: Boolean) {
        viewModelScope.launch { quiet.set(on) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
