package com.wander.mono.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.system.SystemContrast
import com.wander.mono.core.system.Wallpaper
import com.wander.mono.core.system.WallpaperTone
import com.wander.mono.data.settings.AccessibilitySettings
import com.wander.mono.data.settings.HomeLookSettings
import com.wander.mono.data.settings.LIST_POSITIONS
import com.wander.mono.data.settings.RowDensity
import com.wander.mono.data.settings.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * What home needs to stay legible: the wallpaper's tone, whether the user wants a text shadow over
 * no scrim, whether high-contrast text is on (Mono's setting or Android's), how roomy the rows are
 * and where a short list sits ([listPosition], percent of the free room above it).
 */
data class HomeLook(
    val tone: WallpaperTone?,
    val textShadow: Boolean,
    val highContrast: Boolean = false,
    val density: RowDensity = RowDensity.Default,
    val listPosition: Int = LIST_POSITIONS.last,
)

@HiltViewModel
class HomeLookViewModel @Inject constructor(
    wallpaper: Wallpaper,
    settings: SettingsStore,
    systemContrast: SystemContrast,
) : ViewModel() {
    private val highContrast = combine(
        settings[AccessibilitySettings.HighContrastText],
        systemContrast.raised,
    ) { own, system -> own || system }

    val look: StateFlow<HomeLook> = combine(
        wallpaper.tone,
        settings[HomeLookSettings.TextShadow],
        highContrast,
        settings[HomeLookSettings.Density],
        settings[HomeLookSettings.ListPosition],
        ::HomeLook,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeLook(null, textShadow = false))

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
