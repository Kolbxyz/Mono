package com.wander.mono.ui.home

import com.wander.mono.core.calendar.CalendarAgenda
import com.wander.mono.core.notifications.NotificationPreview
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.data.LiveRowKind
import com.wander.mono.data.LiveRowRef

/** The previews whose app (in that profile) the user did not switch the preview row off for. */
internal fun Map<PackageUser, NotificationPreview>.unlessHidden(
    hidden: Set<LiveRowRef>,
): Map<PackageUser, NotificationPreview> {
    val off = hidden.filter { it.kind == LiveRowKind.NotificationPreview }
        .mapTo(HashSet()) { PackageUser(it.app.packageName, it.app.userSerial) }
    return if (off.isEmpty()) this else filterKeys { it !in off }
}

/** [this], or null when the user switched the calendar row off for the calendar app. */
internal fun CalendarAgenda?.unlessHidden(hidden: Set<LiveRowRef>): CalendarAgenda? {
    val agenda = this ?: return null
    val off = hidden.any { it.kind == LiveRowKind.Calendar && it.app.packageName == agenda.appPackage }
    return if (off) null else agenda
}
