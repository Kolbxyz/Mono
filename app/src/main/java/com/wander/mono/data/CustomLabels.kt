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

private val Context.labelStore by preferencesDataStore(name = "labels")

/** Names the user gave apps, per app key. Kept after uninstall, like favourites. */
@Singleton
class CustomLabels @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.labelStore

    val labels: Flow<Map<AppKey, String>> = store.data
        .map { prefs ->
            prefs.asMap().entries.mapNotNull { (key, value) ->
                val app = AppKey.decode(key.name) ?: return@mapNotNull null
                (value as? String)?.let { app to it }
            }.toMap()
        }
        .distinctUntilChanged()

    /** A blank [label] goes back to the app's own name. */
    suspend fun setLabel(app: AppKey, label: String?) {
        val key = stringPreferencesKey(app.encode())
        val trimmed = label?.trim().orEmpty()
        store.edit { prefs -> if (trimmed.isEmpty()) prefs.remove(key) else prefs[key] = trimmed }
    }

    /** Replaces every custom name, for a restored backup; blank names are skipped as in [setLabel]. */
    suspend fun replace(labels: Map<AppKey, String>) {
        store.edit { prefs ->
            prefs.clear()
            labels.forEach { (app, label) ->
                label.trim().takeIf { it.isNotEmpty() }?.let { prefs[stringPreferencesKey(app.encode())] = it }
            }
        }
    }
}
