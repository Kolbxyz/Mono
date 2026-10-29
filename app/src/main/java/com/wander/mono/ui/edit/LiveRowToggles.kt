package com.wander.mono.ui.edit

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.calendar.CalendarAgenda
import com.wander.mono.core.media.MediaState
import com.wander.mono.core.notifications.NotificationPreview
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.data.LiveRowKind
import com.wander.mono.data.LiveRowRef

/** A live row switch under a favourite in edit mode. */
data class LiveRowToggle(val row: LiveRowRef, val shown: Boolean)

/**
 * The live row switches of each favourite: one per kind the app has right now ([available], real
 * data only) or that the user switched off (so it can be switched back on), in [LiveRowKind] order.
 */
internal fun liveRowToggles(
    favorites: List<LaunchableApp>,
    available: Map<AppKey, Set<LiveRowKind>>,
    hidden: Set<LiveRowRef>,
): Map<AppKey, List<LiveRowToggle>> = favorites.associate { app ->
    val kinds = LiveRowKind.entries.filter { kind ->
        kind in available[app.key].orEmpty() || LiveRowRef(app.key, kind) in hidden
    }
    app.key to kinds.map { kind ->
        val row = LiveRowRef(app.key, kind)
        LiveRowToggle(row, shown = row !in hidden)
    }
}

/**
 * Which favourites have a now-playing row to switch: those of the playing app. Sessions are only
 * readable for Mono's own profile, so only main-profile entries own one.
 */
internal fun nowPlayingAvailability(media: MediaState, favorites: List<LaunchableApp>): Map<AppKey, Set<LiveRowKind>> {
    if (media !is MediaState.Active) return emptyMap()
    return favorites
        .filter { it.isMainProfile && it.key.packageName == media.media.packageName }
        .associate { it.key to setOf(LiveRowKind.NowPlaying) }
}

/** Which favourites have a notification preview row to switch: those with a notification now. */
internal fun previewAvailability(
    previews: Map<PackageUser, NotificationPreview>,
    favorites: List<LaunchableApp>,
): Map<AppKey, Set<LiveRowKind>> = favorites
    .filter { PackageUser(it.key.packageName, it.key.userSerial) in previews }
    .associate { it.key to setOf(LiveRowKind.NotificationPreview) }

/** Which favourites have a calendar row to switch: the main-profile calendar app, when it has events. */
internal fun calendarAvailability(
    agenda: CalendarAgenda?,
    favorites: List<LaunchableApp>,
): Map<AppKey, Set<LiveRowKind>> {
    if (agenda == null || agenda.events.isEmpty()) return emptyMap()
    return favorites
        .filter { it.isMainProfile && it.key.packageName == agenda.appPackage }
        .associate { it.key to setOf(LiveRowKind.Calendar) }
}

/** The kinds each app has, from several availability maps. */
internal fun mergeAvailability(vararg maps: Map<AppKey, Set<LiveRowKind>>): Map<AppKey, Set<LiveRowKind>> =
    maps.flatMap { it.entries }.groupBy({ it.key }, { it.value }).mapValues { (_, kinds) -> kinds.flatten().toSet() }
