package com.wander.mono.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.wander.mono.ui.drawer.DrawerViewModel

/**
 * Back on home has nothing to go back to; it only ever closes what is open: a typed [query] first
 * (in the drawer, or the [wide] layout's side panel), then the drawer, then edit mode. Settings, the
 * drawer and the sheets take back first with their own predictive handlers.
 */
@Composable
fun LauncherBack(
    launcher: LauncherViewModel,
    drawer: DrawerViewModel,
    query: String,
    drawerOpen: Boolean,
    editing: Boolean,
    wide: Boolean,
) {
    BackHandler {
        when {
            query.isNotEmpty() && (drawerOpen || wide) -> drawer.setQuery("")
            drawerOpen -> launcher.closeDrawer()
            editing -> launcher.setEditing(false)
        }
    }
}
