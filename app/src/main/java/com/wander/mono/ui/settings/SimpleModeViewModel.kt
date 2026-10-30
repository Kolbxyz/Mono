package com.wander.mono.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.data.settings.AccessibilitySettings
import com.wander.mono.data.settings.HomeLookSettings
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.data.settings.SimpleLook
import com.wander.mono.data.settings.simpleModeWrites
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The large, simple preset's switch: one tap sets (or puts back) density, label size and contrast
 * in a single edit. Switches are taken one at a time, so two quick taps never keep the preset as
 * the user's own look.
 */
@HiltViewModel
class SimpleModeViewModel @Inject constructor(private val store: SettingsStore) : ViewModel() {
    private val switching = Mutex()

    val on: Flow<Boolean> = store[AccessibilitySettings.SimpleMode]

    /** The stored value as last read, for the row's first frame. */
    fun current(): Boolean? = store.current(AccessibilitySettings.SimpleMode)

    fun setOn(on: Boolean) {
        viewModelScope.launch {
            switching.withLock {
                val current = SimpleLook(
                    store[HomeLookSettings.Density].first(),
                    store[HomeLookSettings.LabelScale].first(),
                    store[AccessibilitySettings.HighContrastText].first(),
                )
                val kept = SimpleLook(
                    store[AccessibilitySettings.KeptDensity].first(),
                    store[AccessibilitySettings.KeptLabelScale].first(),
                    store[AccessibilitySettings.KeptHighContrast].first(),
                )
                val wasOn = store[AccessibilitySettings.SimpleMode].first()
                store.restore(simpleModeWrites(on, wasOn, current, kept))
            }
        }
    }
}
