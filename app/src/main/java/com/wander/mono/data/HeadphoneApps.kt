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

private val Context.headphoneStore by preferencesDataStore(name = "headphones")

/**
 * The favourites that move to the top of home while headphones are connected. A file of its own,
 * like quiet apps, so it never touches the saved home order. Keys of apps removed from home are
 * kept: adding the app back brings the choice back.
 */
@Singleton
class HeadphoneApps @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.headphoneStore

    val apps: Flow<Set<AppKey>> = store.data
        .map { prefs -> prefs[APPS].orEmpty().mapNotNull(AppKey::decode).toSet() }
        .distinctUntilChanged()

    suspend fun setLifted(key: AppKey, lifted: Boolean) {
        store.edit { prefs ->
            val current = prefs[APPS].orEmpty()
            prefs[APPS] = if (lifted) current + key.encode() else current - key.encode()
        }
    }

    /** Restores a backup's choice in one edit. */
    suspend fun replace(keys: Set<AppKey>) {
        store.edit { prefs -> prefs[APPS] = keys.map { it.encode() }.toSet() }
    }

    private companion object {
        val APPS = stringSetPreferencesKey("headphone_apps")
    }
}
