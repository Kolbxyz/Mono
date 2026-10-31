package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.system.SystemTime
import java.util.Calendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * The one app the drawer offers above its search field while nothing is typed: the one opened most
 * around this hour ([com.wander.mono.data.LaunchHours]), the hour itself counting double next to
 * the hours either side. Only a habit: an app opened fewer than [MIN_LAUNCHES] times around now is
 * never offered, so with too little history there is no suggestion at all.
 */
internal object AppPrediction {
    const val MIN_LAUNCHES = 5
    private const val HOURS = 24

    /** The best of [candidates] for [hour] in [counts] (by hour, then app); null when none qualifies. */
    fun mostLikely(counts: Map<Int, Map<AppKey, Int>>, hour: Int, candidates: Collection<AppKey>): AppKey? {
        val before = counts[(hour + HOURS - 1) % HOURS].orEmpty()
        val now = counts[hour].orEmpty()
        val after = counts[(hour + 1) % HOURS].orEmpty()
        return candidates
            .map { key ->
                val atHour = now[key] ?: 0
                val around = (before[key] ?: 0) + atHour + (after[key] ?: 0)
                Scored(key, launches = around, score = around + atHour)
            }
            .filter { it.launches >= MIN_LAUNCHES }
            // Ties go the same way every time, never flickering between two apps.
            .maxWithOrNull(compareBy<Scored> { it.score }.thenByDescending { it.key.encode() })
            ?.key
    }

    /** The entry of [apps] to suggest at [hour]; null when none qualifies. */
    fun entryFor(apps: List<DrawerEntry>, counts: Map<Int, Map<AppKey, Int>>, hour: Int): DrawerEntry? {
        val key = mostLikely(counts, hour, apps.map { it.app.key }) ?: return null
        return apps.firstOrNull { it.app.key == key }
    }

    private data class Scored(val key: AppKey, val launches: Int, val score: Int)
}

/** The hour of the day (0 to 23), from the system's minute tick and time changes: no timer of its own. */
internal fun hourOfDay(time: SystemTime): Flow<Int> = time.now
    .map { millis -> Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.HOUR_OF_DAY) }
    .distinctUntilChanged()
