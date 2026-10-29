package com.wander.mono.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.system.Wallpaper
import com.wander.mono.core.system.WallpaperTone
import com.wander.mono.data.HomeLookPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** What home needs to stay legible: the wallpaper's tone, and whether the user wants a text shadow over no scrim. */
data class HomeLook(val tone: WallpaperTone?, val textShadow: Boolean)

@HiltViewModel
class HomeLookViewModel @Inject constructor(wallpaper: Wallpaper, prefs: HomeLookPrefs) : ViewModel() {
    val look: StateFlow<HomeLook> = combine(wallpaper.tone, prefs.textShadow, ::HomeLook)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeLook(null, textShadow = false))

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
