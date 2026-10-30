package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.components.AppBadge

/** [fresh]: installed in the last few days and not opened since, marked with a dot in the list. */
data class DrawerEntry(val app: LaunchableApp, val badge: AppBadge, val fresh: Boolean = false)
