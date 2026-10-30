package com.wander.mono.core.security

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

private val Context.lockedAppsStore by preferencesDataStore(name = "locked_apps")

/**
 * The apps that open only after the screen lock. A file of its own, like quiet apps; which of
 * them are unlocked right now is [AppLock]'s, in memory only.
 */
@Singleton
class LockedApps @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.lockedAppsStore

    val apps: Flow<Set<AppKey>> = store.data
        .map { prefs -> prefs[LOCKED].orEmpty().mapNotNull(AppKey::decode).toSet() }
        .distinctUntilChanged()

    suspend fun setLocked(key: AppKey, locked: Boolean) {
        store.edit { prefs ->
            val current = prefs[LOCKED].orEmpty()
            prefs[LOCKED] = if (locked) current + key.encode() else current - key.encode()
        }
    }

    /** Restores a backup's choice in one edit. */
    suspend fun replace(keys: Set<AppKey>) {
        store.edit { prefs -> prefs[LOCKED] = keys.map { it.encode() }.toSet() }
    }

    private companion object {
        val LOCKED = stringSetPreferencesKey("locked_apps")
    }
}
