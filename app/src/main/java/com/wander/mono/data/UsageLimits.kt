package com.wander.mono.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
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

private val Context.limitStore by preferencesDataStore(name = "usage_limits")

/**
 * A daily time limit per app, in minutes, chosen from its options. Past it, home dims the
 * favourite; nothing is blocked. Only apps with a limit are stored.
 */
@Singleton
class UsageLimits @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.limitStore

    val limits: Flow<Map<AppKey, Int>> = store.data
        .map { it.readLimits() }
        .distinctUntilChanged()

    /** A null [minutes] removes the limit. */
    suspend fun set(app: AppKey, minutes: Int?) {
        store.edit { prefs ->
            val others = prefs.readLimits() - app
            prefs[LIMITS] = (if (minutes == null) others else others + (app to minutes)).encodeAll()
        }
    }

    /** Replaces every limit, for a restored backup. */
    suspend fun replace(limits: Map<AppKey, Int>) {
        store.edit { prefs -> prefs[LIMITS] = limits.encodeAll() }
    }

    private fun Preferences.readLimits(): Map<AppKey, Int> =
        this[LIMITS].orEmpty().mapNotNull(LimitEntry::decode).toMap()

    private fun Map<AppKey, Int>.encodeAll(): Set<String> =
        entries.mapTo(mutableSetOf()) { (app, minutes) -> LimitEntry.encode(app, minutes) }

    companion object {
        /** The limits the options offer, in minutes. */
        val CHOICES: List<Int> = listOf(15, 30, 60, 90, 120, 180)

        private val LIMITS = stringSetPreferencesKey("daily_limits")
    }
}

/** One app's limit as persisted: the app key, '|', then whole minutes above zero. */
internal object LimitEntry {
    private const val SEPARATOR = '|'

    fun encode(app: AppKey, minutes: Int): String = "${app.encode()}$SEPARATOR$minutes"

    fun decode(encoded: String): Pair<AppKey, Int>? {
        val app = AppKey.decode(encoded.substringBefore(SEPARATOR))
        val minutes = encoded.substringAfter(SEPARATOR, "").toIntOrNull()?.takeIf { it > 0 }
        return if (app != null && minutes != null) app to minutes else null
    }
}
