package com.wander.mono.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.R
import com.wander.mono.core.system.BundledWallpaper
import com.wander.mono.core.system.WallpaperSetter
import com.wander.mono.core.system.WallpaperTarget
import com.wander.mono.data.settings.AccessibilitySettings
import com.wander.mono.data.settings.PresetLook
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.data.settings.ThemePreset
import com.wander.mono.data.settings.presetLook
import com.wander.mono.data.settings.presetWrites
import com.wander.mono.di.AppScope
import com.wander.mono.ui.MessageAction
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The Looks and Wallpapers sections: applying a [ThemePreset] in one edit, and setting a bundled
 * wallpaper. A wallpaper is written on the app's scope, so leaving settings (or tapping the
 * snackbar after it closed) never cuts a write short; the result comes back as a snackbar.
 */
@HiltViewModel
class LooksViewModel @Inject constructor(
    private val store: SettingsStore,
    private val wallpapers: WallpaperSetter,
    private val messages: UserMessages,
    @AppScope private val appScope: CoroutineScope,
) : ViewModel() {
    private val applying = Mutex()

    /** The settings a preset shapes, as they are now; null until first read. */
    val current: StateFlow<PresetLook?> =
        store.presetLook().stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    /**
     * Puts on [preset] in one edit, then offers its wallpaper in the snackbar (home screen only:
     * the lock screen is the user's to change, from the Wallpapers section).
     */
    fun apply(preset: ThemePreset) {
        viewModelScope.launch {
            applying.withLock {
                // The large, simple preset owns row density while it is on.
                val simple = store[AccessibilitySettings.SimpleMode].first()
                store.restore(presetWrites(preset.look, keepDensity = simple))
            }
            val offer = preset.wallpaper?.let { wallpaper ->
                MessageAction(R.string.theme_preset_set_wallpaper) { setWallpaper(wallpaper, WallpaperTarget.Home) }
            }
            messages.show(R.string.theme_preset_applied, offer)
        }
    }

    fun setWallpaper(wallpaper: BundledWallpaper, target: WallpaperTarget) {
        appScope.launch {
            if (messages.report(wallpapers.apply(wallpaper, target))) messages.show(R.string.wallpaper_applied)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
