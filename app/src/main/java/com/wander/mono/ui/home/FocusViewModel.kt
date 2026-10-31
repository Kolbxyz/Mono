package com.wander.mono.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.Focus
import com.wander.mono.data.FocusedFavorite
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Focusing a favourite from its sheet, for an hour, or ending that focus early. */
@HiltViewModel
class FocusViewModel @Inject constructor(private val focus: FocusedFavorite) : ViewModel() {
    /** The favourite focused on now; an end already passed reads as none. */
    val focused: StateFlow<AppKey?> = focus.stored
        .map { it?.takeIf { stored -> stored.until > System.currentTimeMillis() }?.app }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    fun focusForAnHour(key: AppKey) {
        viewModelScope.launch { focus.start(Focus(key, System.currentTimeMillis() + FOCUS_MS)) }
    }

    fun end() {
        viewModelScope.launch { focus.end() }
    }

    private companion object {
        const val FOCUS_MS = 60 * 60 * 1_000L
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
