package com.wander.mono.ui.home

import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.notifications.LiveActivity
import com.wander.mono.core.notifications.LiveActivityKind
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.data.LiveRowRef
import com.wander.mono.data.rowKind

/**
 * An ongoing notification of the favourite [app] (a download, a timer, a route), right under it.
 * Keyed by the app and the kind, not the notification, so each update redraws the row in place.
 */
data class LiveActivityRow(val app: LaunchableApp, val activity: LiveActivity) : HomeRow {
    override val key: String get() = "live:${activity.kind.name}:${app.key.encode()}"
}

/**
 * Puts each favourite's live activities right under it, in [LiveActivityKind] order; apps that are
 * not favourites get none, as with previews.
 */
internal fun withLiveActivities(rows: List<HomeRow>, activities: List<LiveActivity>): List<HomeRow> {
    if (activities.isEmpty()) return rows
    val byOwner = activities.groupBy { it.owner }
    return rows.flatMap { row ->
        val app = (row as? HomeRow.App)?.app ?: return@flatMap listOf(row)
        val own = byOwner[PackageUser(app.key.packageName, app.key.userSerial)].orEmpty()
        listOf(row) + own.sortedBy { it.kind }.map { LiveActivityRow(app, it) }
    }
}

/** The activities whose kind is switched on in settings and whose row the user did not switch off for that app. */
internal fun liveActivitiesShown(
    all: List<LiveActivity>,
    hidden: Set<LiveRowRef>,
    kinds: Set<LiveActivityKind>,
): List<LiveActivity> = all.filter { activity ->
    activity.kind in kinds && hidden.none { ref ->
        ref.kind == activity.kind.rowKind &&
            ref.app.packageName == activity.owner.packageName &&
            ref.app.userSerial == activity.owner.userSerial
    }
}
