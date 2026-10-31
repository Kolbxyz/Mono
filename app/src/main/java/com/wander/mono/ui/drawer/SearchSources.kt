package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.ShortcutRepository
import com.wander.mono.core.contacts.Contact
import com.wander.mono.core.contacts.ContactSearch
import com.wander.mono.core.notifications.NotificationPreview
import com.wander.mono.core.notifications.NotificationPreviews
import com.wander.mono.core.siblings.WandaSearch
import com.wander.mono.core.siblings.WandaTrack
import com.wander.mono.core.system.ClockActions
import com.wander.mono.core.system.SettingsPage
import com.wander.mono.core.system.SettingsPages
import com.wander.mono.core.system.WebSearch
import com.wander.mono.core.system.WebSearchTarget
import com.wander.mono.data.settings.SearchSettings
import com.wander.mono.data.settings.SettingsStore
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest

/**
 * Search results beyond apps (shortcuts, sums, conversions, timers and alarms, notes and calls,
 * links, contacts, notifications, settings pages, the web); tapping most of them goes to
 * [SourceOpener], timers and alarms to [ClockActions], hand-offs to [SearchHandOffs]. A source the user switched off in
 * [SearchSettings] is not asked at all.
 */
class SearchSources @Inject constructor(
    private val settings: SettingsStore,
    private val shortcutRepository: ShortcutRepository,
    private val contactSearch: ContactSearch,
    private val wandaSearch: WandaSearch,
    private val settingsPages: SettingsPages,
    private val webSearch: WebSearch,
    private val clock: ClockActions,
    private val previews: NotificationPreviews,
    private val handOffApps: SearchHandOffs,
) {
    /**
     * Contacts for each [query], asked of the provider only while the setting is on; a newer query
     * cancels the lookup still running for the previous one. Tagged with the query they answer.
     */
    fun contacts(query: Flow<String>): Flow<QueryResults<Contact>> =
        combine(query.map { it.trim() }, settings[SearchSettings.Contacts], ::Pair)
            .distinctUntilChanged()
            .mapLatest { (text, enabled) ->
                // "call Anna" looks for Anna: the contact to call ([CommandQuery]).
                val name = CommandQuery.callee(text) ?: text
                val found = if (enabled && text.isNotEmpty()) contactSearch.find(name, PROVIDER_LIMIT) else emptyList()
                QueryResults(text, found)
            }

    /** Songs from Wanda's library for each [query]; empty when Wanda is absent or not signed like Mono. */
    fun tracks(query: Flow<String>): Flow<QueryResults<WandaTrack>> = query.map { it.trim() }
        .distinctUntilChanged()
        .mapLatest { text -> QueryResults(text, wandaSearch.find(text, PROVIDER_LIMIT)) }

    /** Every app's shortcuts while the setting is on; none, and the repository unwatched, while off. */
    fun shortcuts(): Flow<List<AppShortcut>> = settings[SearchSettings.Shortcuts]
        .flatMapLatest { enabled -> if (enabled) shortcutRepository.all else flowOf(emptyList<AppShortcut>()) }

    /** Whether quick answers (sums, conversions, timers and alarms) are on; [actions] is then asked. */
    fun quickAnswers(): Flow<Boolean> = settings[SearchSettings.QuickAnswers]

    /**
     * What [query] asks for ([SearchActions]) when quick answers are [enabled]; a timer or an alarm
     * only when a clock app takes it, a number to call only when a dialer does. Resolves intents for
     * those, so not on the main thread.
     */
    fun actions(query: String, enabled: Boolean): List<SearchHit> {
        if (!enabled) return emptyList()
        return SearchActions.of(query).filter { hit ->
            when (hit) {
                is SearchHit.Timer -> clock.canSetTimer()
                is SearchHit.Alarm -> clock.canSetAlarm()
                is SearchHit.Dial -> handOffApps.canDial()
                else -> true
            }
        }
    }

    /**
     * The query as a link to open and as text to share ([SearchLinks]) when quick answers are
     * [enabled]; a link only when an app opens it, so this resolves an intent: not on the main thread.
     */
    fun handOffs(query: String, enabled: Boolean): List<SearchHit> {
        if (!enabled) return emptyList()
        return SearchLinks.of(query).filter { it !is SearchHit.Link || handOffApps.canOpen(it) }
    }

    /**
     * The posted notifications search looks in while the setting is on: each app's latest, with the
     * conversations of its expanded preview, already redacted as home shows them. None while off.
     */
    fun notices(): Flow<List<NotificationPreview>> = settings[SearchSettings.Notifications].flatMapLatest { on ->
        if (on) previews.previews.map { it.values.toList() } else flowOf(emptyList<NotificationPreview>())
    }

    /**
     * The settings pages this phone can open while the setting is on, resolved afresh each time
     * search starts being watched or the setting comes on.
     */
    fun pages(): Flow<List<SettingsPage>> = settings[SearchSettings.SettingsPages].flatMapLatest { enabled ->
        if (enabled) {
            flow { emit(settingsPages.available()) }.flowOn(Dispatchers.Default)
        } else {
            flowOf(emptyList<SettingsPage>())
        }
    }

    fun webTarget(): WebSearchTarget? = webSearch.target()

    private companion object {
        /** More than are shown, so ranking by name still has a choice after the provider's own matching. */
        const val PROVIDER_LIMIT = 20
    }
}
