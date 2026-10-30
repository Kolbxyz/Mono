package com.wander.mono.ui.sheet

import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.LaunchableApp

/**
 * What the long-press sheet offers for [app]. [isFavorite]: it is on the home list.
 * [liftsWithHeadphones] is null when the choice is not offered: the app is not a favourite, or
 * the headphones setting is off. [isLocked]: it opens only after the screen lock.
 */
data class AppSheetState(
    val app: LaunchableApp,
    val isFavorite: Boolean,
    val isHidden: Boolean,
    val shortcuts: List<AppShortcut>,
    val liftsWithHeadphones: Boolean?,
    val isLocked: Boolean,
) {
    /** Private space apps never go on home: they would show there while the space is locked. */
    val canGoOnHome: Boolean get() = !app.isPrivate
}
