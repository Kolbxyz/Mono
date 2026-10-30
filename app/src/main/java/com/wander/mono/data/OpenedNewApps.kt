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

private val Context.openedNewStore by preferencesDataStore(name = "opened_new_apps")

/**
 * Newly installed apps the user has opened since, so the drawer stops calling them new. A state of
 * the moment, not a choice: in its own small file, left out of backups. Only keys still new are
 * kept, so the set never grows past the handful of apps installed in the last few days.
 */
@Singleton
class OpenedNewApps @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.openedNewStore

    val opened: Flow<Set<AppKey>> = store.data
        .map { prefs -> prefs[OPENED].orEmpty().mapNotNull(AppKey::decode).toSet() }
        .distinctUntilChanged()

    /** Records [key] as opened, dropping every recorded key not in [stillNew]. */
    suspend fun markOpened(key: AppKey, stillNew: Set<AppKey>) {
        store.edit { prefs ->
            val kept = prefs[OPENED].orEmpty().filter { encoded -> AppKey.decode(encoded) in stillNew }
            prefs[OPENED] = kept.toSet() + key.encode()
        }
    }

    private companion object {
        val OPENED = stringSetPreferencesKey("opened")
    }
}
