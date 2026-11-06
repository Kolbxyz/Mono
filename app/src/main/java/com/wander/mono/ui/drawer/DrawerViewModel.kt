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
import com.wander.mono.core.apps.ShortcutIcons
import com.wander.mono.core.apps.WorkProfile
import com.wander.mono.core.apps.WorkProfiles
import com.wander.mono.core.contacts.ContactReach
import com.wander.mono.core.notifications.NotificationCounts
import com.wander.mono.core.notifications.NotificationPreviews
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.core.security.HiddenAppsLock
import com.wander.mono.core.siblings.PhalanxRooms
import com.wander.mono.core.system.SystemContrast
import com.wander.mono.core.system.Wallpaper
import com.wander.mono.data.LauncherPrefs
import com.wander.mono.data.MutedApps
import com.wander.mono.data.OpenedNewApps
import com.wander.mono.data.RecentSearch
import com.wander.mono.data.RecentSearches
import com.wander.mono.data.settings.AccessibilitySettings
import com.wander.mono.data.settings.BadgeSettings
import com.wander.mono.data.settings.DrawerSettings
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val catalog: AppCatalog,
    prefs: LauncherPrefs,
    settings: SettingsStore,
    counts: NotificationCounts,
    previews: NotificationPreviews,
    phalanx: PhalanxRooms,
    private val actions: AppActions,
    private val icons: IconCache,
    private val messages: UserMessages,
    private val sources: SearchSources,
    private val opener: SearchOpener,
    private val memory: SearchMemory,
    private val hiddenLock: HiddenAppsLock,
    private val privateSpace: PrivateSpace,
    private val workProfiles: WorkProfiles,
    private val shortcutIcons: ShortcutIcons,
    wallpaper: Wallpaper,
    systemContrast: SystemContrast,
    openedNew: OpenedNewApps,
    muted: MutedApps,
    private val recorder: LaunchRecorder,
) : ViewModel() {
    /** Kept in saved state, so a half-typed search survives rotation and process death. */
    val query: StateFlow<String> = savedState.getStateFlow(QUERY, "")

    private val listChoices = ListChoiceState(savedState, settings, openedNew, viewModelScope)

    /** What the drawer's list options menu and category chips change. */
    val listActions: DrawerListActions = listChoices.actions

    private val entries = combine(
        catalog.apps.filterNotNull(),
        counts.counts,
        settings[BadgeSettings.ShowCounts],
        shownPreviews(previews, settings, muted),
    ) { apps, badges, showCounts, shown ->
        apps.map { app ->
            val owner = PackageUser(app.key.packageName, app.key.userSerial)
            DrawerEntry(app, appBadge(badges[owner] ?: 0, showCounts), preview = shown[owner])
        }
    }

    /** What search looks in besides the apps themselves. */
    private val searchable = searchableFor(sources, memory, query)

    private val hidden = combine(prefs.hidden, hiddenLock.revealed, ::HiddenApps)

    private val profiled = combine(entries, workProfiles.all, ::Profiled)

    private val highContrast = combine(
        settings[AccessibilitySettings.HighContrastText],
        systemContrast.raised,
    ) { own, system -> own || system }

    private val look = combine(settings[DrawerSettings.Background], wallpaper.tone, highContrast, ::DrawerLook)

    val state: StateFlow<DrawerUiState> = combine(
        profiled,
        hidden,
        searchable,
        query,
        combine(settings[IconSettings.Mode], settings[IconSettings.Pack], look, ::Triple),
    ) { (all, work), hidden, extra, q, (mode, pack, drawerLook) ->
        val lists = drawerLists(all, hidden.keys, work)
        DrawerUiState(
            apps = lists.apps,
            sections = sectionsOf(lists.apps.map { it.app.label }),
            results = SearchResults.assemble(
                q,
                sources.actions(q, extra.more.quickAnswers),
                hidden.searchableApps(searchableApps(all, work)),
                hidden.searchableShortcuts(searchableShortcuts(extra.shortcuts, work)),
                extra.contacts.itemsFor(q),
                extra.tracks.itemsFor(q),
                extra.pages,
                handOffs = sources.handOffs(q, extra.more.quickAnswers),
                notices = extra.more.notices,
                preferred = RecentSearches.preferredFor(extra.more.recent, q),
                webTarget = sources::webTarget,
            ),
            resultsQuery = builtFor(q, extra.contacts, extra.tracks),
            iconMode = mode,
            iconPack = pack,
            hiddenLocked = hidden.locked,
            privateApps = lists.privateApps,
            work = lists.work,
            look = drawerLook,
        )
    }
        .combine(privateSpace.state) { state, space -> state.withPrivateSpace(space) }
        .combine(listChoices.inputs(catalog.apps.filterNotNull())) { state, (choices, opened, now) ->
            state.shaped(choices, opened, now)
        }
        .combine(settings[DrawerSettings.RailAtStart]) { state, railAtStart -> state.copy(railAtStart = railAtStart) }
        .combine(conversationUnread(previews, phalanx, settings)) { state, unread ->
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

    /** [icon], if it is in memory already: for a row's first frame. */
    fun cachedIcon(key: AppKey, mode: IconMode, sizePx: Int): AppIconImage? =
        icons.cached(key, themed = mode == IconMode.Themed, sizePx)

    /** The shortcut's own icon (a face, an avatar); null when it has none, so the row shows its app's. */
    suspend fun shortcutIcon(shortcut: AppShortcut, sizePx: Int): AppIconImage? = shortcutIcons.icon(shortcut, sizePx)

    fun setPrivateLocked(locked: Boolean) {
        messages.report(privateSpace.setLocked(locked))
    }

    fun openPrivateSettings() {
        messages.report(privateSpace.openSettings())
    }

    fun setWorkPaused(profile: WorkProfile, paused: Boolean) {
        messages.report(workProfiles.setPaused(profile, paused))
    }

    /** True when the app opened, so the caller can close the drawer. */
    fun launch(app: LaunchableApp): Boolean = messages.report(actions.launch(app.key)).also { opened ->
        if (opened) {
            listChoices.opened(app, catalog.apps.value.orEmpty())
            recorder.opened(app)
        }
    }

    /** The drawer closed: the next opening starts on every app again. */
    fun clearListFilters() = listChoices.clear()

    fun launch(hit: SearchHit): HitOutcome = opener.open(hit, query.value)

    /** A contact's Call, Message or Email action. */
    fun reach(hit: SearchHit.Person, how: ContactReach): HitOutcome = opener.reach(hit, how, query.value)

    /** Searches remembered while the setting is on, offered as chips while the field is empty. */
    val recentSearches: StateFlow<List<RecentSearch>> = memory.recent
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    fun forgetRecent(search: RecentSearch) {
        viewModelScope.launch { memory.forget(search) }
    }

    /**
     * The keyboard's action key hands [onTop] the top (highlighted) result, to act on as a tap would,
     * once the results for what is typed are in, never the previous query's; typing on before then
     * cancels it. While [picking] an app for home it is the top of the list shown then.
     */
    fun withTopResult(picking: Boolean, onTop: (SearchHit) -> Unit) {
        val typed = query.value
        viewModelScope.launch {
            val built = state.first { it.resultsQuery == typed || query.value != typed }
            val shown = if (picking) pickable(built.results) else built.results
            shown.firstOrNull().takeIf { built.resultsQuery == typed && query.value == typed }?.let(onTop)
        }
    }

    fun unlockHidden() = hiddenLock.unlock()

    fun relockHidden() = hiddenLock.relock()

    fun reportNoScreenLock() {
        messages.report(ActionResult.Failed(R.string.error_no_screen_lock))
    }

    private companion object {
        const val QUERY = "query"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
