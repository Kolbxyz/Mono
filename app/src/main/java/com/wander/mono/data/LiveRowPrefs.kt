package com.wander.mono.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.liveRowStore by preferencesDataStore(name = "live_rows")

/**
 * Which live rows the user switched off, per app and row kind. Every live row shows by default,
 * so only the exceptions are stored. New row kinds plug in by adding a [LiveRowKind]. Also where
 * the user placed the now-playing card, once moved in edit mode.
 */
@Singleton
class LiveRowPrefs @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.liveRowStore

    val hidden: Flow<Set<LiveRowRef>> = store.data
        .map { prefs -> prefs[HIDDEN].orEmpty().mapNotNull(LiveRowRef::decode).toSet() }
        .distinctUntilChanged()

    suspend fun setShown(row: LiveRowRef, shown: Boolean) {
        store.edit { prefs ->
            val current = prefs[HIDDEN].orEmpty()
            prefs[HIDDEN] = if (shown) current - row.encode() else current + row.encode()
        }
    }

    /** Replaces every switched-off row, for a restored backup. */
    suspend fun replace(hidden: Set<LiveRowRef>) {
        store.edit { prefs -> prefs[HIDDEN] = hidden.map { it.encode() }.toSet() }
    }

    /** Where the user placed the now-playing card; null while it follows the app that plays. */
    val nowPlayingAnchor: Flow<NowPlayingAnchor?> = store.data
        .map { prefs -> prefs[NOW_PLAYING]?.let(NowPlayingAnchor::decode) }
        .distinctUntilChanged()

    /** Places the now-playing card, or with null lets it follow its app again. */
    suspend fun setNowPlayingAnchor(anchor: NowPlayingAnchor?) {
        store.edit { prefs ->
            if (anchor == null) {
                prefs.remove(NOW_PLAYING)
            } else {
                prefs[NOW_PLAYING] = anchor.encode()
            }
        }
    }

    private companion object {
        val HIDDEN = stringSetPreferencesKey("hidden")
        val NOW_PLAYING = stringPreferencesKey("now_playing_anchor")
    }
}
