package com.wander.mono.ui.sheet

import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.LaunchableApp

/**
 * What the long-press sheet offers for [app]. [favoriteIndex] is -1 when it is not on the home list.
 * [liftsWithHeadphones] is null when the choice is not offered: the app is not a favourite, or
 * the headphones setting is off. [isLocked]: it opens only after the screen lock.
 */
data class AppSheetState(
    val app: LaunchableApp,
    val favoriteIndex: Int,
    val favoriteCount: Int,
    val isHidden: Boolean,
    val shortcuts: List<AppShortcut>,
    val liftsWithHeadphones: Boolean?,
    val isLocked: Boolean,
) {
    val isFavorite: Boolean get() = favoriteIndex >= 0

    /** Private space apps never go on home: they would show there while the space is locked. */
    val canGoOnHome: Boolean get() = !app.isPrivate
    val canMoveUp: Boolean get() = favoriteIndex > 0
    val canMoveDown: Boolean get() = isFavorite && favoriteIndex < favoriteCount - 1
}
