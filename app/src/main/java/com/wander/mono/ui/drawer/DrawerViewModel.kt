package com.wander.mono.ui.drawer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.AppActions
import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.AppIconImage
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.IconCache
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.apps.ShortcutRepository
import com.wander.mono.core.notifications.NotificationCounts
import com.wander.mono.core.notifications.NotificationPreviews
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.data.LauncherPrefs
import com.wander.mono.data.settings.BadgeSettings
import com.wander.mono.data.settings.IconMode
import com.wander.mono.data.settings.IconSettings
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.ui.UserMessages
import com.wander.mono.ui.components.appBadge
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    catalog: AppCatalog,
    prefs: LauncherPrefs,
    settings: SettingsStore,
    shortcuts: ShortcutRepository,
    counts: NotificationCounts,
    previews: NotificationPreviews,
    private val actions: AppActions,
    private val icons: IconCache,
    private val messages: UserMessages,
) : ViewModel() {
    /** Kept in saved state, so a half-typed search survives rotation and process death. */
    val query: StateFlow<String> = savedState.getStateFlow(QUERY, "")

    private val entries = combine(
        catalog.apps.filterNotNull(),
        counts.counts,
        settings[BadgeSettings.ShowCounts],
    ) { apps, badges, showCounts ->
        apps.map { app ->
            val count = badges[PackageUser(app.key.packageName, app.key.userSerial)] ?: 0
            DrawerEntry(app, appBadge(count, showCounts))
        }
    }

    val state: StateFlow<DrawerUiState> = combine(
        entries,
        prefs.hidden,
        shortcuts.all,
        query,
        settings[IconSettings.Mode],
    ) { all, hidden, sc, q, mode ->
        val visible = all.filter { it.app.key !in hidden }
        DrawerUiState(
            apps = visible,
            sections = sectionsOf(visible.map { it.app.label }),
            results = AppSearch.search(q, all, sc),
            iconMode = mode,
        )
    }
        .combine(combine(previews.conversations, settings[BadgeSettings.ShowCounts], ::Pair)) { state, unread ->
            state.copy(results = state.results.withUnread(unread.first, unread.second))
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DrawerUiState())

    fun setQuery(text: String) {
        savedState[QUERY] = text
    }

    suspend fun icon(key: AppKey, mode: IconMode, sizePx: Int): AppIconImage? =
        icons.icon(key, themed = mode == IconMode.Themed, sizePx)

    /** True when the app opened, so the caller can close the drawer. */
    fun launch(app: LaunchableApp): Boolean = messages.report(actions.launch(app.key))

    fun launch(hit: SearchHit): Boolean = when (hit) {
        is SearchHit.App -> launch(hit.entry.app)
        is SearchHit.Shortcut -> messages.report(actions.launchShortcut(hit.shortcut))
    }

    /** The keyboard's action key opens the best match. */
    fun launchTopResult(): Boolean = state.value.results.firstOrNull()?.let(::launch) ?: false

    private companion object {
        const val QUERY = "query"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
