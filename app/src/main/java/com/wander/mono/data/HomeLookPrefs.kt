package com.wander.mono.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.homeLookStore by preferencesDataStore(name = "home_look")

/** How home draws over the wallpaper. */
@Singleton
class HomeLookPrefs @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.homeLookStore

    /** A shadow under home text instead of the surface scrim, for users who want the wallpaper untouched. */
    val textShadow: Flow<Boolean> = store.data
        .map { it[TEXT_SHADOW] ?: false }
        .distinctUntilChanged()

    suspend fun setTextShadow(enabled: Boolean) {
        store.edit { it[TEXT_SHADOW] = enabled }
    }

    private companion object {
        val TEXT_SHADOW = booleanPreferencesKey("text_shadow")
    }
}
