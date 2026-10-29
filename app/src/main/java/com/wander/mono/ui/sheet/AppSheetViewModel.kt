package com.wander.mono.ui.sheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppActions
import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.ActionResult
import com.wander.mono.core.apps.ShortcutRepository
import com.wander.mono.data.LauncherPrefs
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The long-press sheet. Its target is kept in saved state, so an open sheet survives rotation. */
@HiltViewModel
class AppSheetViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    catalog: AppCatalog,
    shortcuts: ShortcutRepository,
    private val prefs: LauncherPrefs,
    private val actions: AppActions,
    private val messages: UserMessages,
) : ViewModel() {
    private val target: StateFlow<String?> = savedState.getStateFlow(TARGET, null)

    /** Null when no sheet is open, or its app has just been uninstalled. */
    val state: StateFlow<AppSheetState?> = combine(
        target,
        catalog.apps,
        prefs.favorites,
        prefs.hidden,
        shortcuts.all,
    ) { encoded, apps, favorites, hidden, allShortcuts ->
        val key = encoded?.let(AppKey::decode) ?: return@combine null
        val app = apps?.firstOrNull { it.key == key } ?: return@combine null
        AppSheetState(
            app = app,
            favoriteIndex = favorites.indexOf(key),
            favoriteCount = favorites.size,
            isHidden = key in hidden,
            shortcuts = allShortcuts.filter { it.packageName == key.packageName && it.userSerial == key.userSerial },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    fun show(key: AppKey) {
        savedState[TARGET] = key.encode()
    }

    fun dismiss() {
        savedState[TARGET] = null
    }

    fun addToHome(key: AppKey) = edit { prefs.addFavorite(key) }

    fun removeFromHome(key: AppKey) = edit { prefs.removeFavorite(key) }

    /** Keeps the sheet open, so the app can be moved several places in a row. */
    fun move(key: AppKey, by: Int) {
        viewModelScope.launch { prefs.moveFavorite(key, by) }
    }

    fun setHidden(key: AppKey, hidden: Boolean) = edit { prefs.setHidden(key, hidden) }

    fun openAppInfo(key: AppKey) = finish(actions.openAppInfo(key))

    fun uninstall(key: AppKey) = finish(actions.uninstall(key))

    fun launchShortcut(shortcut: AppShortcut) = finish(actions.launchShortcut(shortcut))

    private fun edit(change: suspend () -> Unit) {
        dismiss()
        viewModelScope.launch { change() }
    }

    private fun finish(result: ActionResult) {
        dismiss()
        messages.report(result)
    }

    private companion object {
        const val TARGET = "sheet_target"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
