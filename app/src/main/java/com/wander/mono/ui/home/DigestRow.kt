package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.notifications.NotificationPreview
import com.wander.mono.core.notifications.PackageUser

/**
 * "While you were away", first on home once quiet mode ends: the [apps] that posted during it, most
 * first, and how many notifications came in all ([count]). Its [previews] are those apps' latest
 * notifications, shown when it is tapped open; empty while previews are off.
 */
data class DigestRow(val apps: List<LaunchableApp>, val count: Int, val previews: List<NotificationRow>) : HomeRow {
    override val key: String get() = "digest"
}

/**
 * The digest of a quiet session that just ended, from the counts it gathered ([away]): apps muted on
 * home are left out, as are posters with nothing to launch (system services). Null when nothing is left.
 */
internal fun digestRow(
    away: Map<PackageUser, Int>,
    catalog: List<LaunchableApp>,
    muted: Set<AppKey>,
    previews: Map<PackageUser, NotificationPreview>,
): DigestRow? {
    val silenced = muted.mapTo(HashSet()) { PackageUser(it.packageName, it.userSerial) }
    val byOwner = catalog.groupBy { PackageUser(it.key.packageName, it.key.userSerial) }
    val shown = away.entries
        .filter { (owner, _) -> owner !in silenced }
        .sortedByDescending { it.value }
        .mapNotNull { (owner, count) -> byOwner[owner]?.firstOrNull()?.let { app -> Triple(owner, app, count) } }
    return if (shown.isEmpty()) {
        null
    } else {
        DigestRow(
            apps = shown.map { it.second },
            count = shown.sumOf { it.third },
            previews = shown.mapNotNull { (owner, app, _) -> previews[owner]?.let { NotificationRow(app, it) } },
        )
    }
}

/**
 * Home with the [digest] first, above every other row. A preview already shown under its favourite
 * is left out of the digest's, so the same notification never shows (and expands) twice.
 */
internal fun withDigest(rows: List<HomeRow>, digest: DigestRow?): List<HomeRow> {
    if (digest == null) return rows
    val inline = rows.mapNotNullTo(HashSet()) { (it as? NotificationRow)?.key }
    return listOf(digest.copy(previews = digest.previews.filter { it.key !in inline })) + rows
}
