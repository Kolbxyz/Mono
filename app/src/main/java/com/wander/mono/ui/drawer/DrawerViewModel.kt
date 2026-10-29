package com.wander.mono.ui.drawer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import com.wander.mono.core.apps.AppActions
import com.wander.mono.core.apps.AppCatalog
import com.wander.mono.core.apps.AppIconImage
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.IconCache
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.apps.PrivateSpace
import com.wander.mono.core.apps.ShortcutRepository
import com.wander.mono.core.contacts.Contact
import com.wander.mono.core.notifications.ConversationKey
import com.wander.mono.core.notifications.NotificationCounts
import com.wander.mono.core.notifications.NotificationPreviews
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.core.security.HiddenAppsLock
import com.wander.mono.core.siblings.PhalanxRooms
import com.wander.mono.core.siblings.PhalanxUnread
import com.wander.mono.core.siblings.WandaTrack
import com.wander.mono.core.system.ClockActions
import com.wander.mono.core.system.SettingsPage
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
    phalanx: PhalanxRooms,
    private val actions: AppActions,
    private val icons: IconCache,
    private val messages: UserMessages,
    private val sources: SearchSources,
    private val clock: ClockActions,
    private val hiddenLock: HiddenAppsLock,
    private val privateSpace: PrivateSpace,
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

    /** What search looks in besides the apps themselves. */
    private val searchable = combine(
        shortcuts.all,
        sources.contacts(query),
        sources.tracks(query),
        sources.pages(),
        ::Searchable,
    )

    private val hidden = combine(prefs.hidden, hiddenLock.revealed, ::HiddenApps)

    val state: StateFlow<DrawerUiState> = combine(
        entries,
        hidden,
        searchable,
        query,
        settings[IconSettings.Mode],
    ) { all, hidden, extra, q, mode ->
        val lists = drawerLists(all, hidden.keys)
        DrawerUiState(
            apps = lists.apps,
            sections = sectionsOf(lists.apps.map { it.app.label }),
            results = SearchResults.assemble(
                q,
                sources.actions(q),
                hidden.searchableApps(all),
                hidden.searchableShortcuts(extra.shortcuts),
                extra.contacts,
                extra.tracks,
                extra.pages,
                sources::webTarget,
            ),
            iconMode = mode,
            hiddenLocked = hidden.locked,
            privateApps = lists.privateApps,
        )
    }
        .combine(privateSpace.state) { state, space -> state.copy(privateSpace = space) }
        .combine(
            combine(previews.conversations, phalanx.unread, settings[BadgeSettings.ShowCounts], ::ShortcutUnread),
        ) { state, unread ->
            val results = state.results.withUnread(unread.notified, unread.showCounts)
                .withRoomUnread(unread.phalanx, unread.showCounts)
            state.copy(results = results)
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

    fun launch(hit: SearchHit): HitOutcome = when (hit) {
        is SearchHit.App -> opened(launch(hit.entry.app))
        is SearchHit.Shortcut -> opened(messages.report(actions.launchShortcut(hit.shortcut)))
        is SearchHit.Calculation -> sources.copy(hit)
        is SearchHit.Timer -> opened(messages.report(clock.setTimer(hit.seconds)))
        is SearchHit.Alarm -> opened(messages.report(clock.setAlarm(hit.time.hour, hit.time.minute)))
        is SearchHit.Person -> sources.open(hit)
        is SearchHit.Track -> sources.open(hit)
        is SearchHit.Setting -> sources.open(hit)
        is SearchHit.Web -> sources.open(hit)
    }

    /** The keyboard's action key does what tapping the top (highlighted) result does. */
    fun launchTopResult(): HitOutcome = state.value.results.firstOrNull()?.let(::launch) ?: HitOutcome.Stayed

    private fun opened(done: Boolean) = if (done) HitOutcome.Opened else HitOutcome.Stayed

    private data class Searchable(
        val shortcuts: List<AppShortcut>,
        val contacts: List<Contact>,
        val tracks: List<WandaTrack>,
        val pages: List<SettingsPage>,
    )

    fun unlockHidden() = hiddenLock.unlock()

    fun relockHidden() = hiddenLock.relock()

    fun setPrivateLocked(locked: Boolean) {
        messages.report(privateSpace.setLocked(locked))
    }

    fun openPrivateSettings() {
        messages.report(privateSpace.openSettings())
    }

    fun reportNoScreenLock() {
        messages.report(ActionResult.Failed(R.string.error_no_screen_lock))
    }

    /** Unread per conversation from notifications, and from Phalanx itself when it publishes it. */
    private data class ShortcutUnread(
        val notified: Map<ConversationKey, Int>,
        val phalanx: PhalanxUnread?,
        val showCounts: Boolean,
    )

    private companion object {
        const val QUERY = "query"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
