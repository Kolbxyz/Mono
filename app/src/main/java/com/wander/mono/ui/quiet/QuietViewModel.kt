package com.wander.mono.ui.quiet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.quiet.QuietMode
import com.wander.mono.core.system.SystemContrast
import com.wander.mono.core.system.Wallpaper
import com.wander.mono.data.settings.AccessibilitySettings
import com.wander.mono.data.settings.DrawerSettings
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.ui.drawer.DrawerLook
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Quiet mode for home's options and the drawer's gate. */
@HiltViewModel
class QuietViewModel @Inject constructor(
    private val quiet: QuietMode,
    settings: SettingsStore,
    wallpaper: Wallpaper,
    systemContrast: SystemContrast,
) : ViewModel() {
    val enabled: StateFlow<Boolean> = quiet.enabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    /** What the gate is drawn on: the drawer's own background, read the way the drawer reads it. */
    val drawerLook: StateFlow<DrawerLook> = combine(
        settings[DrawerSettings.Background],
        wallpaper.tone,
        combine(settings[AccessibilitySettings.HighContrastText], systemContrast.raised) { own, raised ->
            own || raised
        },
        ::DrawerLook,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DrawerLook())

    fun setEnabled(on: Boolean) {
        viewModelScope.launch { quiet.set(on) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
