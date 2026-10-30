package com.wander.mono.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.wander.mono.di.AppScope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val Context.settingsStore by preferencesDataStore(name = "settings")

/**
 * Every [Setting], in a DataStore file of its own: the home list in `LauncherPrefs` is data, these
 * are preferences, and keeping them apart lets each feature add a key without touching a shared file.
 */
@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext context: Context,
    @AppScope scope: CoroutineScope,
) {
    private val store = context.settingsStore

    /**
     * The file as DataStore last delivered it, held from launch on, so a settings page opens on its
     * values instead of filling in a frame later. Null only until the first read.
     */
    private val loaded: StateFlow<Preferences?> = store.data.stateIn(scope, SharingStarted.Eagerly, null)

    // Straight from DataStore, not the snapshot: a read right after a write must see the write.
    operator fun <T> get(setting: Setting<T>): Flow<T> = store.data
        .map { setting.read(it) }
        .distinctUntilChanged()

    /** [setting]'s value as last read, for a row's first frame; null only before the file's first read. */
    fun <T> current(setting: Setting<T>): T? = loaded.value?.let(setting::read)

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
