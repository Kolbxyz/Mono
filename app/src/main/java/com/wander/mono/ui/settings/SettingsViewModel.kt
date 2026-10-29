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

    fun <T> set(setting: Setting<T>, value: T) {
        viewModelScope.launch { store.set(setting, value) }
    }
}

/** The stored value of [setting]; null until it has been read, so no row shows a guessed value. */
@Composable
fun <T : Any> SettingsViewModel.collect(setting: Setting<T>): T? =
    remember(setting) { observe(setting) }.collectAsStateWithLifecycle(null).value
