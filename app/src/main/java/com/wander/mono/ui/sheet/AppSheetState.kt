package com.wander.mono.ui.sheet

import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.LaunchableApp

/**
 * What the long-press sheet offers for [app]. [favoriteIndex] is -1 when it is not on the home list.
 * [liftsWithHeadphones] is null when the choice is not offered: the app is not a favourite, or
 * the headphones setting is off.
 */
data class AppSheetState(
    val app: LaunchableApp,
    val favoriteIndex: Int,
    val favoriteCount: Int,
    val isHidden: Boolean,
    val shortcuts: List<AppShortcut>,
    val liftsWithHeadphones: Boolean?,
) {
    val isFavorite: Boolean get() = favoriteIndex >= 0
    val canMoveUp: Boolean get() = favoriteIndex > 0
    val canMoveDown: Boolean get() = isFavorite && favoriteIndex < favoriteCount - 1
}
