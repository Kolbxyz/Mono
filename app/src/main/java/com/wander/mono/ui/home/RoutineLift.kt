package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.Routine
import com.wander.mono.data.appsAt
import com.wander.mono.data.millisToNextBoundary
import java.time.LocalTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.transformLatest

/**
 * The favourites the routines on right now put first on home. Worked out at once, then again only
 * at the next start or end of a routine (one delay to it, never a timer), and whenever
 * [clockChanged] says the clock was set or the zone moved; its receiver is only registered while
 * there is a routine. Home stops collecting while it is away, so a boundary passed during sleep is
 * caught as it comes back.
 */
internal fun routineApps(routines: Flow<List<Routine>>, clockChanged: Flow<Long>): Flow<Set<AppKey>> =
    routines.flatMapLatest { all ->
        if (all.isEmpty()) return@flatMapLatest flowOf(emptySet<AppKey>())
        clockChanged.transformLatest {
            while (true) {
                val now = LocalTime.now()
                emit(all.appsAt(now))
                delay(all.millisToNextBoundary(now) ?: break)
            }
        }
    }.distinctUntilChanged()
