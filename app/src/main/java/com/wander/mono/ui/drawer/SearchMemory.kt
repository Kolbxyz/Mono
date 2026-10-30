package com.wander.mono.ui.drawer

import com.wander.mono.data.RecentSearch
import com.wander.mono.data.SearchHistory
import com.wander.mono.data.settings.SearchSettings
import com.wander.mono.data.settings.SettingsStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Search's memory of what it opened, only while the user asks for it: nothing is written, and
 * nothing read back, while [SearchSettings.RememberRecent] is off.
 */
class SearchMemory @Inject constructor(
    private val settings: SettingsStore,
    private val history: SearchHistory,
) {
    /** The remembered searches, newest first; none while the setting is off. */
    val recent: Flow<List<RecentSearch>> = settings[SearchSettings.RememberRecent].flatMapLatest { on ->
        if (on) history.recent else flowOf(emptyList<RecentSearch>())
    }

    /**
     * Remembers that [query] opened [hit]. Not a notification, which is gone once read, nor a link
     * or shared text, whose query is the pasted text itself.
     */
    suspend fun remember(query: String, hit: SearchHit) {
        if (!rememberable(hit) || !settings[SearchSettings.RememberRecent].first()) return
        history.remember(RecentSearch(query, hit.key))
    }

    suspend fun forget(search: RecentSearch) = history.forget(search)

    private fun rememberable(hit: SearchHit): Boolean = when (hit) {
        is SearchHit.Notice, is SearchHit.Link, is SearchHit.ShareText -> false
        else -> true
    }
}
