package com.wander.mono.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wander.mono.core.apps.AppKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.focusStore by preferencesDataStore(name = "focus")

/** The favourite home is focused on, until [until] (epoch millis). */
data class Focus(val app: AppKey, val until: Long)

/**
 * The favourite the user focused on from its sheet, and when that ends: saved only so a focus
 * survives the process being stopped. A state of the moment: its own small file, never backed up.
 * The stored focus may have ended already; whoever shows it compares [Focus.until] with the clock.
 */
@Singleton
class FocusedFavorite @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.focusStore

    val stored: Flow<Focus?> = store.data
        .map { prefs -> prefs[APP]?.let(AppKey::decode)?.let { app -> Focus(app, prefs[UNTIL] ?: 0L) } }
        .distinctUntilChanged()

    suspend fun start(focus: Focus) {
        store.edit { prefs ->
            prefs[APP] = focus.app.encode()
            prefs[UNTIL] = focus.until
        }
    }

    suspend fun end() {
        store.edit { prefs ->
            prefs.remove(APP)
            prefs.remove(UNTIL)
        }
    }

    private companion object {
        val APP = stringPreferencesKey("focus_app")
        val UNTIL = longPreferencesKey("focus_until")
    }
}
