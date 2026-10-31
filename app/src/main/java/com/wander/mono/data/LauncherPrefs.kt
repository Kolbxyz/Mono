package com.wander.mono.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wander.mono.core.apps.AppKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.launcherStore by preferencesDataStore(name = "launcher")

/**
 * The user's home list and hidden apps. An ordered list and a set of a few dozen keys: DataStore
 * holds that in one small file with no schema or migrations to maintain, which Room would add for
 * nothing. Keys of uninstalled apps are kept, so reinstalling an app puts it back where it was.
 */
@Singleton
class LauncherPrefs @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.launcherStore

    val favorites: Flow<List<AppKey>> = store.data
        .map { it.readFavorites() }
        .distinctUntilChanged()

    val hidden: Flow<Set<AppKey>> = store.data
        .map { prefs -> prefs[HIDDEN].orEmpty().mapNotNull(AppKey::decode).toSet() }
        .distinctUntilChanged()

    suspend fun addFavorite(key: AppKey) = editFavorites { if (key in it) it else it + key }

    suspend fun removeFavorite(key: AppKey) = editFavorites { it - key }

    /** Puts [key] back on home at [index] (last, if the list has shrunk since), for Undo; once only. */
    suspend fun insertFavorite(key: AppKey, index: Int) = editFavorites {
        if (key in it) it else it.toMutableList().apply { add(index.coerceIn(0, size), key) }
    }

    suspend fun moveFavorite(key: AppKey, by: Int) = editFavorites { it.moved(key, by) }

    /** Saves a drag in edit mode: [order] is the shown favourites, in their new order. */
    suspend fun reorderFavorites(order: List<AppKey>) = editFavorites { it.reordered(order) }

    suspend fun setHidden(key: AppKey, hidden: Boolean) {
        store.edit { prefs ->
            val current = prefs[HIDDEN].orEmpty()
            prefs[HIDDEN] = if (hidden) current + key.encode() else current - key.encode()
        }
    }

    /** Replaces the home list and the hidden apps at once, for a restored backup. */
    suspend fun replace(favorites: List<AppKey>, hidden: Set<AppKey>) {
        store.edit { prefs ->
            prefs.writeFavorites(favorites.distinct())
            prefs[HIDDEN] = hidden.map { it.encode() }.toSet()
        }
    }

    private suspend fun editFavorites(change: (List<AppKey>) -> List<AppKey>) {
        store.edit { prefs -> prefs.writeFavorites(change(prefs.readFavorites())) }
    }

    private fun Preferences.readFavorites(): List<AppKey> =
        this[FAVORITES].orEmpty().lines().mapNotNull(AppKey::decode).distinct()

    private fun MutablePreferences.writeFavorites(keys: List<AppKey>) {
        this[FAVORITES] = keys.joinToString("\n") { it.encode() }
    }

    private companion object {
        val FAVORITES = stringPreferencesKey("favorites")
        val HIDDEN = stringSetPreferencesKey("hidden")
    }
}
