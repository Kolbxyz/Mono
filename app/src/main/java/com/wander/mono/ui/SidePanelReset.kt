package com.wander.mono.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.wander.mono.ui.drawer.DrawerViewModel

/**
 * The [wide] layout's side panel never leaves, so its closing is where the next opening starts
 * afresh: no query, hidden apps locked again, no list filters, nothing being picked.
 */
@Composable
fun SidePanelReset(wide: Boolean, drawerOpen: Boolean, launcher: LauncherViewModel, drawer: DrawerViewModel) {
    LaunchedEffect(wide, drawerOpen) {
        if (wide && !drawerOpen) {
            drawer.setQuery("")
            drawer.relockHidden()
            drawer.clearListFilters()
            launcher.drawerClosed()
        }
    }
}
