package com.wander.mono.data.settings

import com.wander.mono.core.notifications.LiveActivityKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Which ongoing notifications become live rows under their app; each can also be switched off per app. */
object LiveActivitySettings {
    /** Downloads, deliveries, Android 16 Live Updates: anything under way with a progress. */
    val LiveUpdates = BooleanSetting("live_rows_live_updates", default = true)

    /** Timers and stopwatches, counting while home shows them. */
    val Timers = BooleanSetting("live_rows_timers", default = true)

    /** The next turn of a route being followed. */
    val Navigation = BooleanSetting("live_rows_navigation", default = true)
}

/** The kinds of live row switched on. */
fun SettingsStore.liveActivityKinds(): Flow<Set<LiveActivityKind>> = combine(
    this[LiveActivitySettings.LiveUpdates],
    this[LiveActivitySettings.Timers],
    this[LiveActivitySettings.Navigation],
) { progress, timers, navigation ->
    buildSet {
        if (progress) add(LiveActivityKind.Progress)
        if (timers) add(LiveActivityKind.Timer)
        if (navigation) add(LiveActivityKind.Navigation)
    }
}
