package com.wander.mono.ui.sheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.data.CustomLabels
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The rename dialog, opened from the app sheet or edit mode. Its target survives rotation. */
@HiltViewModel
class RenameViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    catalog: AppCatalog,
    private val labels: CustomLabels,
) : ViewModel() {
    private val target: StateFlow<String?> = savedState.getStateFlow(TARGET, null)

    /** The app being renamed; null when the dialog is closed or the app was just uninstalled. */
    val app: StateFlow<LaunchableApp?> = combine(target, catalog.apps) { encoded, apps ->
        val key = encoded?.let(AppKey::decode) ?: return@combine null
        apps?.firstOrNull { it.key == key }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    fun show(key: AppKey) {
        savedState[TARGET] = key.encode()
    }

    fun dismiss() {
        savedState[TARGET] = null
    }

    /** Null or blank goes back to the app's own name. */
    fun rename(key: AppKey, label: String?) {
        dismiss()
        viewModelScope.launch { labels.setLabel(key, label) }
    }

    private companion object {
        const val TARGET = "rename_target"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
