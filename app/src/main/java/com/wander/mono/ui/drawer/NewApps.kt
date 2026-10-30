package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import java.util.concurrent.TimeUnit

/** How long a newly installed app counts as new, unless opened first. */
internal val NEW_FOR_MS = TimeUnit.DAYS.toMillis(NEW_FOR_DAYS)

private const val NEW_FOR_DAYS = 3L

/**
 * [app] was first installed less than [NEW_FOR_MS] before [now] and has not been [opened] since.
 * An archived app is never new: it was installed long ago and only offloaded.
 */
internal fun isNew(app: LaunchableApp, now: Long, opened: Set<AppKey>): Boolean {
    val installed = app.installedAt ?: return false
    return !app.archived && app.key !in opened && now - installed in 0 until NEW_FOR_MS
}

/**
 * When the next of [installTimes] stops counting as new, after [now]; null when none will. The
 * drawer re-reads its list then, the one moment newness changes without an event.
 */
internal fun nextNewExpiry(installTimes: List<Long>, now: Long): Long? =
    installTimes.map { it + NEW_FOR_MS }.filter { it > now }.minOrNull()
