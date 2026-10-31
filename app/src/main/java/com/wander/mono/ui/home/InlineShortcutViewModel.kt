package com.wander.mono.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppActions
import com.wander.mono.core.apps.AppIconImage
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.ShortcutIcons
import com.wander.mono.core.apps.ShortcutRepository
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * The shortcuts a swipe right unfolds under a favourite (dynamic, manifest, pinned and cached, from
 * `LauncherApps`), one favourite at a time. Kept in memory only: home opens with them folded.
 */
@HiltViewModel
class InlineShortcutViewModel @Inject constructor(
    repository: ShortcutRepository,
    private val actions: AppActions,
    private val icons: ShortcutIcons,
    private val messages: UserMessages,
) : ViewModel() {
    private val openApp = MutableStateFlow<AppKey?>(null)

    /** The favourite whose shortcuts are unfolded, if any. */
    val open: StateFlow<AppKey?> = openApp.asStateFlow()

    /** Every app's shortcuts, by package and profile; an app's activities all share them. */
    internal val byApp: StateFlow<Map<ShortcutOwner, List<AppShortcut>>> = repository.all
        .map { all -> all.groupBy { ShortcutOwner(it.packageName, it.userSerial) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private fun shortcutsOf(app: AppKey): List<AppShortcut> = byApp.value[ShortcutOwner.of(app)].orEmpty()

    /**
     * A swipe right on [app]: folds its shortcuts when they are open, else unfolds them in place of
     * any other's. False when the app has none, so nothing opens.
     */
    fun toggle(app: AppKey): Boolean = when {
        openApp.value == app -> {
            openApp.value = null
            true
        }
        shortcutsOf(app).isEmpty() -> false
        else -> {
            openApp.value = app
            true
        }
    }

    fun close() {
        openApp.value = null
    }

    /** Goes through the app lock like any other launch; the row folds behind it. */
    fun launch(shortcut: AppShortcut) {
        close()
        messages.report(actions.launchShortcut(shortcut))
    }

    /** The shortcut's own icon (a face, an avatar); null when it has none, so its chip shows its app's. */
    suspend fun icon(shortcut: AppShortcut, sizePx: Int): AppIconImage? = icons.icon(shortcut, sizePx)
}

/** Whose shortcuts they are: a package in one profile. */
internal data class ShortcutOwner(val packageName: String, val userSerial: Long) {
    companion object {
        fun of(app: AppKey) = ShortcutOwner(app.packageName, app.userSerial)
    }
}
