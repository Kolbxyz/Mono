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
import com.wander.mono.core.usage.AppUsage
import com.wander.mono.data.HeadphoneApps
import com.wander.mono.data.LauncherPrefs
import com.wander.mono.data.settings.HeadphoneSettings
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.data.settings.UsageSettings
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
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
    settings: SettingsStore,
    private val usage: AppUsage,
    private val headphoneApps: HeadphoneApps,
) : ViewModel() {
    private val target: StateFlow<String?> = savedState.getStateFlow(TARGET, null)

    /** Null when no sheet is open, or its app has just been uninstalled. */
    val state: StateFlow<AppSheetState?> = combine(
        target,
        catalog.apps,
        prefs.favorites,
        combine(prefs.hidden, headphoneChoices(settings, headphoneApps), ::Pair),
        shortcuts.all,
    ) { encoded, apps, favorites, (hidden, lifting), allShortcuts ->
        val key = encoded?.let(AppKey::decode) ?: return@combine null
        val app = apps?.firstOrNull { it.key == key } ?: return@combine null
        AppSheetState(
            app = app,
            favoriteIndex = favorites.indexOf(key),
            favoriteCount = favorites.size,
            isHidden = key in hidden,
            shortcuts = allShortcuts.filter { it.packageName == key.packageName && it.userSerial == key.userSerial },
            liftsWithHeadphones = lifting?.takeIf { key in favorites }?.contains(key),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    /** Today's time in the sheet's app, read once as it opens. */
    val usageToday: StateFlow<SheetUsage> = combine(
        state.map { it?.app }.distinctUntilChanged(),
        settings[UsageSettings.ShowInSheet],
    ) { app, show -> app.takeIf { show } }
        .transformLatest { app ->
            // Reading first, so one app's time never shows on another's sheet.
            val readable = app != null && app.isMainProfile && usage.isGranted()
            emit(usageBeforeReading(show = app != null, readable = readable))
            if (app != null && readable) emit(usageRead(usage.todayMillis(app)))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SheetUsage.Hidden)

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

    /** Keeps the sheet open, so the switch is seen to change. */
    fun setLiftsWithHeadphones(key: AppKey, lifts: Boolean) {
        viewModelScope.launch { headphoneApps.setLifted(key, lifts) }
    }

    fun setHidden(key: AppKey, hidden: Boolean) = edit { prefs.setHidden(key, hidden) }

    fun openAppInfo(key: AppKey) = finish(actions.openAppInfo(key))

    fun uninstall(key: AppKey) = finish(actions.uninstall(key))

    fun launchShortcut(shortcut: AppShortcut) = finish(actions.launchShortcut(shortcut))

    // No dismiss() in the actions: the sheet slides itself away, then calls it.
    private fun edit(change: suspend () -> Unit) {
        viewModelScope.launch { change() }
    }

    private fun finish(result: ActionResult) {
        messages.report(result)
    }

    /** The apps chosen for headphones, or null while the setting is off. */
    private fun headphoneChoices(settings: SettingsStore, apps: HeadphoneApps): Flow<Set<AppKey>?> =
        combine(settings[HeadphoneSettings.LiftApps], apps.apps) { on, chosen -> chosen.takeIf { on } }

    private companion object {
        const val TARGET = "sheet_target"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
