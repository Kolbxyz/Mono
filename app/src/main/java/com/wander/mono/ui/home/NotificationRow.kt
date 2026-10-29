package com.wander.mono.ui.home

import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.notifications.NotificationPreview
import com.wander.mono.core.notifications.PackageUser

/**
 * The latest notification of the favourite [app], right under it. Keyed by the app, not the
 * notification, so a newer message updates the row in place instead of replacing it.
 */
data class NotificationRow(val app: LaunchableApp, val preview: NotificationPreview) : HomeRow {
    override val key: String get() = "notification:${app.key.encode()}"
}

/** Puts each favourite's preview right under it; apps that are not favourites get none. */
internal fun withPreviews(rows: List<HomeRow>, previews: Map<PackageUser, NotificationPreview>): List<HomeRow> {
    if (previews.isEmpty()) return rows
    return rows.flatMap { row ->
        val key = (row as? HomeRow.App)?.app?.key
        val preview = key?.let { previews[PackageUser(it.packageName, it.userSerial)] }
        if (row is HomeRow.App && preview != null) listOf(row, NotificationRow(row.app, preview)) else listOf(row)
    }
}
