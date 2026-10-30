package com.wander.mono.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.searchHistoryStore by preferencesDataStore(name = "search_history")

/**
 * The last searches that opened something, newest first ([RecentSearches] keeps the list), kept
 * only while the user asks for it (`SearchSettings.RememberRecent`). A file of its own, never in a
 * backup: what someone searched for is private, and the results it points to (a contact, a
 * shortcut, an app in a work profile) are this phone's own.
 */
@Singleton
class SearchHistory @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.searchHistoryStore

    val recent: Flow<List<RecentSearch>> = store.data
        .map { RecentSearches.decode(it[RECENT].orEmpty()) }
        .distinctUntilChanged()

    suspend fun remember(search: RecentSearch) = edit { RecentSearches.remembered(it, search) }

    suspend fun forget(search: RecentSearch) = edit { list -> list.filterNot { it == search } }

    suspend fun clear() {
        store.edit { it.remove(RECENT) }
    }

    private suspend fun edit(change: (List<RecentSearch>) -> List<RecentSearch>) {
        store.edit { prefs ->
            prefs[RECENT] = RecentSearches.encode(change(RecentSearches.decode(prefs[RECENT].orEmpty())))
        }
    }

    private companion object {
        val RECENT = stringPreferencesKey("recent")
    }
}
