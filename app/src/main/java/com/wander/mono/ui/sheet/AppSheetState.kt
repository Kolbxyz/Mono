package com.wander.mono.ui.sheet

import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.LaunchableApp

/** What the long-press sheet offers for [app]. [favoriteIndex] is -1 when it is not on the home list. */
data class AppSheetState(
    val app: LaunchableApp,
    val favoriteIndex: Int,
    val favoriteCount: Int,
    val isHidden: Boolean,
    val shortcuts: List<AppShortcut>,
) {
    val isFavorite: Boolean get() = favoriteIndex >= 0
    val canMoveUp: Boolean get() = favoriteIndex > 0
    val canMoveDown: Boolean get() = isFavorite && favoriteIndex < favoriteCount - 1
}
