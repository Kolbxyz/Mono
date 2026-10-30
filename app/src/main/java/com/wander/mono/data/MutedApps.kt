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

private val Context.mutedStore by preferencesDataStore(name = "muted")

/**
 * The apps whose notifications home leaves out: no badge, no preview, no live row. The shade and
 * the drawer still show them. A file of its own, like quiet apps; keys are kept after removal.
 */
@Singleton
class MutedApps @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.mutedStore

    val apps: Flow<Set<AppKey>> = store.data
        .map { prefs -> prefs[MUTED].orEmpty().mapNotNull(AppKey::decode).toSet() }
        .distinctUntilChanged()

    suspend fun setMuted(key: AppKey, muted: Boolean) {
        store.edit { prefs ->
            val current = prefs[MUTED].orEmpty()
            prefs[MUTED] = if (muted) current + key.encode() else current - key.encode()
        }
    }

    /** Replaces the muted apps, for a restored backup. */
    suspend fun replace(keys: Set<AppKey>) {
        store.edit { prefs -> prefs[MUTED] = keys.map { it.encode() }.toSet() }
    }

    private companion object {
        val MUTED = stringSetPreferencesKey("muted_on_home")
    }
}
