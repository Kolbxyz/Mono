package com.wander.mono.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.system.GestureActions
import com.wander.mono.data.settings.GestureSettings
import com.wander.mono.data.settings.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Home's system gestures: double tap to lock, swipe down for the notification shade. Each is off
 * until its setting is read; with the gesture service off, using one says so in the snackbar.
 */
@HiltViewModel
class GestureViewModel @Inject constructor(
    private val actions: GestureActions,
    private val messages: UserMessages,
    settings: SettingsStore,
) : ViewModel() {
    val doubleTapLock: StateFlow<Boolean> =
        settings[GestureSettings.DoubleTapLock].stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val swipeDownNotifications: StateFlow<Boolean> =
        settings[GestureSettings.SwipeDownNotifications].stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Whether the user has switched the gesture service on. */
    val available: StateFlow<Boolean> = actions.isAvailable

    fun lockScreen() {
        messages.report(actions.lockScreen())
    }

    fun openNotifications() {
        messages.report(actions.openNotifications())
    }

    fun openAccessSettings() {
        messages.report(actions.openAccessSettings())
    }
}
