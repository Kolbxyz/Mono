package com.wander.mono.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wander.mono.core.apps.AppKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.quietStore by preferencesDataStore(name = "quiet")

/**
 * The favourites that stay on home in quiet mode. A file of its own, like the settings, so quiet
 * mode never touches the home list. Keys of apps removed from home are kept: adding the app back
 * makes it quiet again.
 */
@Singleton
class QuietApps @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.quietStore

    val apps: Flow<Set<AppKey>> = store.data
        .map { prefs -> prefs[QUIET].orEmpty().mapNotNull(AppKey::decode).toSet() }
        .distinctUntilChanged()

    suspend fun setQuiet(key: AppKey, quiet: Boolean) {
        store.edit { prefs ->
            val current = prefs[QUIET].orEmpty()
            prefs[QUIET] = if (quiet) current + key.encode() else current - key.encode()
        }
    }

    /** Replaces the quiet apps, for a restored backup. */
    suspend fun replace(keys: Set<AppKey>) {
        store.edit { prefs -> prefs[QUIET] = keys.map { it.encode() }.toSet() }
    }

    private companion object {
        val QUIET = stringSetPreferencesKey("quiet_apps")
    }
}
