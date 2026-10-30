package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.wander.mono.data.settings.Setting
import com.wander.mono.data.settings.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** Reads and writes any [Setting], so a settings section needs no view model of its own. */
@HiltViewModel
class SettingsViewModel @Inject constructor(private val store: SettingsStore) : ViewModel() {
    fun <T> observe(setting: Setting<T>): Flow<T> = store[setting]

    /** [setting]'s value as last read, so a row has it on the page's first frame. */
    fun <T> current(setting: Setting<T>): T? = store.current(setting)

    fun <T> set(setting: Setting<T>, value: T) {
        viewModelScope.launch { store.set(setting, value) }
    }
}

/**
 * The stored value of [setting], there from the first frame since the store keeps the file read;
 * null only before that first read, so no row ever shows a guessed value.
 */
@Composable
fun <T : Any> SettingsViewModel.collect(setting: Setting<T>): T? {
    val flow = remember(setting) { observe(setting) }
    val initial = remember(setting) { current(setting) }
    return flow.collectAsStateWithLifecycle(initial).value
}
