package com.wander.mono.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore(name = "settings")

/**
 * Every [Setting], in a DataStore file of its own: the home list in `LauncherPrefs` is data, these
 * are preferences, and keeping them apart lets each feature add a key without touching a shared file.
 */
@Singleton
class SettingsStore @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.settingsStore

    operator fun <T> get(setting: Setting<T>): Flow<T> = store.data
        .map { setting.read(it) }
        .distinctUntilChanged()

    suspend fun <T> set(setting: Setting<T>, value: T) {
        store.edit { setting.write(it, value) }
    }

    /** The current value of each of [settings], for a backup. */
    suspend fun snapshot(settings: List<Setting<*>>): List<SettingValue<*>> {
        val prefs = store.data.first()
        return settings.map { SettingValue.of(it, prefs) }
    }

    /** Writes all of [values] in one edit, so a restore never leaves half the settings changed. */
    suspend fun restore(values: List<SettingValue<*>>) {
        store.edit { prefs -> values.forEach { it.writeTo(prefs) } }
    }
}
