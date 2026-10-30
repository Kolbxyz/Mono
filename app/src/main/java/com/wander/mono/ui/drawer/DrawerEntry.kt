package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.notifications.NotificationPreview
import com.wander.mono.ui.components.AppBadge

/**
 * [fresh]: installed in the last few days and not opened since, marked with a dot in the list.
 * [preview]: the app's latest notification, one line under its name; null when it has none, previews
 * are off or the app is muted on home.
 */
data class DrawerEntry(
    val app: LaunchableApp,
    val badge: AppBadge,
    val fresh: Boolean = false,
    val preview: NotificationPreview? = null,
)
