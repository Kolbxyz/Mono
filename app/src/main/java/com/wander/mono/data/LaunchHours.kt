package com.wander.mono.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wander.mono.core.apps.AppKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.launchHoursStore by preferencesDataStore(name = "launch_hours")

/**
 * How often each app was opened (from home, the drawer's list or search) in each hour of the day,
 * so home can point out the likely favourite. A habit, not a choice: its own small file, on this phone only and left out of backups.
 * Apps no longer installed are dropped at the next launch, and an hour's counts halve once one of
 * them reaches [CAP], so old habits fade.
 */
@Singleton
class LaunchHours @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.launchHoursStore

    /** Launch counts by hour of the day (0 to 23), then by app. */
    val counts: Flow<Map<Int, Map<AppKey, Int>>> = store.data.map(::countsOf).distinctUntilChanged()

    /** Counts one launch of [key] in [hour], forgetting apps no longer among the [installed]. */
    suspend fun record(key: AppKey, hour: Int, installed: Set<AppKey>) {
        store.edit { prefs ->
            prefs.asMap().keys.map { it.name }.forEach { name ->
                val app = slotOf(name)?.second
                if (app == null || app !in installed) prefs.remove(intPreferencesKey(name))
            }
            val launched = intPreferencesKey(nameOf(hour, key))
            val next = (prefs[launched] ?: 0) + 1
            prefs[launched] = next
            if (next >= CAP) halve(prefs, hour)
        }
    }

    private fun halve(prefs: MutablePreferences, hour: Int) {
        prefs.asMap().keys.map { it.name }.filter { slotOf(it)?.first == hour }.forEach { name ->
            val key = intPreferencesKey(name)
            val half = (prefs[key] ?: 0) / 2
            if (half > 0) prefs[key] = half else prefs.remove(key)
        }
    }

    private fun countsOf(prefs: Preferences): Map<Int, Map<AppKey, Int>> = prefs.asMap().entries
        .mapNotNull { (key, value) ->
            val (hour, app) = slotOf(key.name) ?: return@mapNotNull null
            (value as? Int)?.let { Triple(hour, app, it) }
        }
        .groupBy({ it.first }, { it.second to it.third })
        .mapValues { (_, launches) -> launches.toMap() }

    private companion object {
        /** A count this high halves its hour's counts. */
        const val CAP = 64
        const val SEPARATOR = ':'

        fun nameOf(hour: Int, key: AppKey) = "$hour$SEPARATOR${key.encode()}"

        /** The hour and app a key [name] counts; neither package nor class names hold a colon. */
        fun slotOf(name: String): Pair<Int, AppKey>? {
            val hour = name.substringBefore(SEPARATOR).toIntOrNull()
            val app = AppKey.decode(name.substringAfter(SEPARATOR))
            return if (hour != null && app != null) hour to app else null
        }
    }
}
