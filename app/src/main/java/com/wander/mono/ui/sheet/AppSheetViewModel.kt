package com.wander.mono.ui.sheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppActions
import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.ActionResult
import com.wander.mono.core.apps.AppIconImage
import com.wander.mono.core.apps.ShortcutIcons
import com.wander.mono.core.apps.ShortcutRepository
import com.wander.mono.core.security.AppLock
import com.wander.mono.core.usage.AppUsage
import com.wander.mono.data.HeadphoneApps
import com.wander.mono.data.LauncherPrefs
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.data.settings.ScreenTimeSettings
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
    private val lock: AppLock,
    private val shortcutIcons: ShortcutIcons,
    private val removals: AppRemovals,
) : ViewModel() {
    private val target: StateFlow<String?> = savedState.getStateFlow(TARGET, null)

    /** Null when no sheet is open, or its app has just been uninstalled. */
    val state: StateFlow<AppSheetState?> = combine(
        target,
        catalog.apps,
        prefs.favorites,
        combine(prefs.hidden, headphoneApps.apps, lock.apps, ::Triple),
        shortcuts.all,
    ) { encoded, apps, favorites, (hidden, lifting, locked), allShortcuts ->
        val key = encoded?.let(AppKey::decode) ?: return@combine null
        val app = apps?.firstOrNull { it.key == key } ?: return@combine null
        AppSheetState(
            app = app,
            isFavorite = key in favorites,
            isHidden = key in hidden,
            shortcuts = allShortcuts.filter { it.packageName == key.packageName && it.userSerial == key.userSerial },
            liftsWithHeadphones = (key in lifting).takeIf { key in favorites },
            isLocked = key in locked,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    /** Today's time in the sheet's app, read once as it opens. */
    val usageToday: StateFlow<SheetUsage> = combine(
        state.map { it?.app }.distinctUntilChanged(),
        settings[ScreenTimeSettings.Nudge],
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

    /** Offers Undo, which puts the favourite back where it was. */
    fun removeFromHome(key: AppKey) = removals.removeFromHome(key)

    /** Keeps the sheet open, so the switch is seen to change. */
    fun setLiftsWithHeadphones(key: AppKey, lifts: Boolean) {
        viewModelScope.launch { headphoneApps.setLifted(key, lifts) }
    }

    /** Hiding offers Undo (spec 7.8): the app is gone from the drawer before the user sees where it went. */
    fun setHidden(key: AppKey, hidden: Boolean) {
        if (hidden) removals.hide(key) else edit { prefs.setHidden(key, hidden = false) }
    }

    /** Keeps the sheet open, so the lock is seen to change. The sheet asks for the screen lock before removing it. */
    fun setLocked(key: AppKey, locked: Boolean) {
        viewModelScope.launch { messages.report(lock.setLocked(key, locked)) }
    }

    /** Goes through the app lock like any other launch. */
    fun open(key: AppKey) = finish(actions.launch(key))

    fun openAppInfo(key: AppKey) = finish(actions.openAppInfo(key))

    fun uninstall(key: AppKey) = finish(actions.uninstall(key))

    fun launchShortcut(shortcut: AppShortcut) = finish(actions.launchShortcut(shortcut))

    /** The shortcut's own icon (a face, an avatar); null when it has none, so its chip shows its app's. */
    suspend fun shortcutIcon(shortcut: AppShortcut, sizePx: Int): AppIconImage? = shortcutIcons.icon(shortcut, sizePx)

    // No dismiss() in the actions: the sheet slides itself away, then calls it.
    private fun edit(change: suspend () -> Unit) {
        viewModelScope.launch { change() }
    }

    private fun finish(result: ActionResult) {
        messages.report(result)
    }

    private companion object {
        const val TARGET = "sheet_target"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
