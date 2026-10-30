package com.wander.mono.ui.drawer

import com.wander.mono.R
import com.wander.mono.core.contacts.Contact
import com.wander.mono.core.contacts.ContactSearch
import com.wander.mono.core.siblings.WandaSearch
import com.wander.mono.core.siblings.WandaTrack
import com.wander.mono.core.system.ClockActions
import com.wander.mono.core.system.Clipboard
import com.wander.mono.core.system.SettingsPage
import com.wander.mono.core.system.SettingsPages
import com.wander.mono.core.system.WebSearch
import com.wander.mono.core.system.WebSearchTarget
import com.wander.mono.data.settings.SearchSettings
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.ui.UserMessages
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest

/**
 * Search results beyond apps and shortcuts (sums, conversions, timers and alarms, contacts, settings
 * pages, the web) and what tapping most of them does; timers and alarms go to [ClockActions].
 */
class SearchSources @Inject constructor(
    private val settings: SettingsStore,
    private val contactSearch: ContactSearch,
    private val wandaSearch: WandaSearch,
    private val settingsPages: SettingsPages,
    private val webSearch: WebSearch,
    private val clock: ClockActions,
    private val clipboard: Clipboard,
    private val messages: UserMessages,
) {
    /**
     * Contacts for each [query], asked of the provider only while the setting is on; a newer query
     * cancels the lookup still running for the previous one. Tagged with the query they answer.
     */
    fun contacts(query: Flow<String>): Flow<QueryResults<Contact>> =
        combine(query.map { it.trim() }, settings[SearchSettings.Contacts], ::Pair)
            .distinctUntilChanged()
            .mapLatest { (text, enabled) ->
                val found = if (enabled && text.isNotEmpty()) contactSearch.find(text, PROVIDER_LIMIT) else emptyList()
                QueryResults(text, found)
            }

    /** Songs from Wanda's library for each [query]; empty when Wanda is absent or not signed like Mono. */
    fun tracks(query: Flow<String>): Flow<QueryResults<WandaTrack>> = query.map { it.trim() }
        .distinctUntilChanged()
        .mapLatest { text -> QueryResults(text, wandaSearch.find(text, PROVIDER_LIMIT)) }

    /**
     * What [query] asks for ([SearchActions]); a timer or an alarm only when a clock app takes it.
     * Resolves intents for those, so not on the main thread.
     */
    fun actions(query: String): List<SearchHit> = SearchActions.of(query).filter { hit ->
        when (hit) {
            is SearchHit.Timer -> clock.canSetTimer()
            is SearchHit.Alarm -> clock.canSetAlarm()
            else -> true
        }
    }

    /** The settings pages this phone can open, resolved afresh each time search starts being watched. */
    fun pages(): Flow<List<SettingsPage>> = flow { emit(settingsPages.available()) }.flowOn(Dispatchers.Default)

    fun webTarget(): WebSearchTarget? = webSearch.target()

    fun copy(hit: SearchHit.Calculation): HitOutcome {
        if (clipboard.copy(R.string.search_calculator_clip, hit.result)) messages.show(R.string.search_copied)
        return HitOutcome.Stayed
    }

    fun open(hit: SearchHit.Person): HitOutcome = opened(messages.report(contactSearch.open(hit.contact)))

    fun open(hit: SearchHit.Setting): HitOutcome = when (hit.page.action) {
        null -> HitOutcome.OpenMonoSettings
        else -> opened(messages.report(settingsPages.open(hit.page)))
    }

    fun open(hit: SearchHit.Track): HitOutcome = opened(messages.report(wandaSearch.open(hit.track)))

    fun open(hit: SearchHit.Web): HitOutcome = opened(messages.report(webSearch.search(hit.query)))

    private fun opened(done: Boolean) = if (done) HitOutcome.Opened else HitOutcome.Stayed

    private companion object {
        /** More than are shown, so ranking by name still has a choice after the provider's own matching. */
        const val PROVIDER_LIMIT = 20
    }
}
