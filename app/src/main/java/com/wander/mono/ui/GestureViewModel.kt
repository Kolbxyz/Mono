package com.wander.mono.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.system.GestureActions
import com.wander.mono.data.settings.GestureSettings
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.data.settings.SwipeDownAction
import com.wander.mono.data.settings.SwipeUpAction
import com.wander.mono.data.settings.swipeDownFromLegacy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Home's gestures: double tap to lock, and what swiping up and down do. Each does nothing until its
 * setting is read; with the gesture service off, one that needs it says so in the snackbar.
 */
@HiltViewModel
class GestureViewModel @Inject constructor(
    private val actions: GestureActions,
    private val messages: UserMessages,
    private val settings: SettingsStore,
) : ViewModel() {
    val doubleTapLock: StateFlow<Boolean> =
        settings[GestureSettings.DoubleTapLock].stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val swipeUp: StateFlow<SwipeUpAction> =
        settings[GestureSettings.SwipeUp].stateIn(viewModelScope, SharingStarted.Eagerly, SwipeUpAction.Drawer)

    val swipeDown: StateFlow<SwipeDownAction> =
        settings[GestureSettings.SwipeDown].stateIn(viewModelScope, SharingStarted.Eagerly, SwipeDownAction.None)

    /** Whether the user has switched the gesture service on. */
    val available: StateFlow<Boolean> = actions.isAvailable

    init {
        // The old swipe-down switch, whenever it reads off: after an update, or a restored older backup.
        viewModelScope.launch {
            settings[GestureSettings.LegacySwipeDownNotifications].collect { on ->
                val carried = swipeDownFromLegacy(on) ?: return@collect
                settings.set(GestureSettings.SwipeDown, carried)
                settings.set(GestureSettings.LegacySwipeDownNotifications, true)
            }
        }
    }

    /** What a swipe down on home runs for [action]; null for none, so home's list may stretch instead. */
    fun pullDownFor(action: SwipeDownAction, openSearch: () -> Unit): (() -> Unit)? = when (action) {
        SwipeDownAction.Notifications -> ::openNotifications
        SwipeDownAction.QuickSettings -> ::openQuickSettings
        SwipeDownAction.Search -> openSearch
        SwipeDownAction.None -> null
    }

    fun lockScreen() {
        messages.report(actions.lockScreen())
    }

    fun openNotifications() {
        messages.report(actions.openNotifications())
    }

    fun openQuickSettings() {
        messages.report(actions.openQuickSettings())
    }

    fun openAccessSettings() {
        messages.report(actions.openAccessSettings())
    }
}
