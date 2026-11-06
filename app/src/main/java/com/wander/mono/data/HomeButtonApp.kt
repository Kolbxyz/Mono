package com.wander.mono.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wander.mono.core.apps.AppKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.homeButtonStore by preferencesDataStore(name = "home_button")

/**
 * The app home's round button opens instead of search, chosen with a long press on it; none means
 * search. Its own small file, not in backups yet.
 */
@Singleton
class HomeButtonApp @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.homeButtonStore

    val stored: Flow<AppKey?> = store.data
        .map { prefs -> prefs[APP]?.let(AppKey::decode) }
        .distinctUntilChanged()

    suspend fun set(app: AppKey) {
        store.edit { it[APP] = app.encode() }
    }

    /** Back to search. */
    suspend fun clear() {
        store.edit { it.remove(APP) }
    }

    private companion object {
        val APP = stringPreferencesKey("home_button_app")
    }
}
