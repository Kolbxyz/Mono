package com.wander.mono.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wander.mono.core.widgets.WidgetPlacement
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.widgetStore by preferencesDataStore(name = "widgets")

/** The widgets placed on home, in order, one encoded [WidgetPlacement] per line; a file of its own. */
@Singleton
class WidgetPrefs @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.widgetStore

    val placements: Flow<List<WidgetPlacement>> = store.data
        .map { it.read() }
        .distinctUntilChanged()

    suspend fun current(): List<WidgetPlacement> = placements.first()

    suspend fun edit(change: (List<WidgetPlacement>) -> List<WidgetPlacement>) {
        store.edit { prefs ->
            prefs[PLACEMENTS] = change(prefs.read()).distinctBy { it.appWidgetId }.joinToString("\n") { it.encode() }
        }
    }

    private fun Preferences.read(): List<WidgetPlacement> =
        this[PLACEMENTS].orEmpty().lines().mapNotNull(WidgetPlacement::decode).distinctBy { it.appWidgetId }

    private companion object {
        val PLACEMENTS = stringPreferencesKey("placements")
    }
}
