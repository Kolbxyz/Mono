package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.calendar.CalendarAgenda
import com.wander.mono.core.media.MediaState
import com.wander.mono.core.notifications.LiveActivity
import com.wander.mono.core.notifications.PackageUser

/**
 * Home without the notifications of the apps the user [muted] there: their counts and previews
 * dropped by owner (package and profile). The shade and the drawer are not filtered.
 */
internal fun <V> Map<PackageUser, V>.unmuted(muted: Set<AppKey>): Map<PackageUser, V> {
    if (muted.isEmpty()) return this
    val off = muted.owners()
    return filterKeys { it !in off }
}

/** The live rows (updates, timers, routes) of apps not muted on home. */
internal fun List<LiveActivity>.unmuted(muted: Set<AppKey>): List<LiveActivity> {
    if (muted.isEmpty()) return this
    val off = muted.owners()
    return filter { it.owner !in off }
}

private fun Set<AppKey>.owners(): Set<PackageUser> = mapTo(HashSet()) { PackageUser(it.packageName, it.userSerial) }

/** [this], or [MediaState.Idle] when the playing app is muted on home (sessions are only Mono's own profile's). */
internal fun MediaState.unlessMuted(muted: Set<AppKey>): MediaState {
    if (this !is MediaState.Active) return this
    return if (muted.any { it.packageName == media.packageName }) MediaState.Idle else this
}

/** [this], or null when the calendar app is muted on home. */
internal fun CalendarAgenda?.unlessMuted(muted: Set<AppKey>): CalendarAgenda? =
    this?.takeUnless { agenda -> muted.any { it.packageName == agenda.appPackage } }
