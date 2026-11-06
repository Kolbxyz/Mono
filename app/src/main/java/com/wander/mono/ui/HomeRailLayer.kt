package com.wander.mono.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.rememberMonoHaptics
import com.wander.mono.ui.drawer.DrawerUiState
import com.wander.mono.ui.drawer.RailScrubStrip
import com.wander.mono.ui.home.HeldLetterGroup
import com.wander.mono.ui.home.HomeButtonRoom
import com.wander.mono.ui.home.HomeRail
import com.wander.mono.ui.home.HomeRailState

/**
 * Home's letter rail over home, under the drawer: the invisible strip down home's edge that a finger
 * lands on while [enabled], the rail it brings out, and the held letter's apps in home's place.
 * Letting go on a letter confirms with a light buzz and opens the drawer in place at that letter
 * ([DrawerMotion.openInPlace], then [onOpenDrawer]); the held letter fades with home behind the
 * drawer as it comes in, and once the drawer covers home, home goes back to showing itself.
 */
@Composable
fun BoxScope.HomeRailLayer(
    rail: HomeRailState,
    drawer: DrawerUiState,
    icons: AppIcons,
    enabled: Boolean,
    drawerMotion: DrawerMotion,
    onOpenDrawer: () -> Unit,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
) {
    val haptics = rememberMonoHaptics()
    LaunchedEffect(rail, drawerMotion) {
        snapshotFlow { drawerMotion.shown >= 1f }.collect { covered -> if (covered) rail.drawerCovers() }
    }
    if (rail.scrub.active || rail.shown > 0) {
        HeldLetterGroup(
            entry = rail.shown,
            drawer = drawer,
            icons = icons,
            onLaunchApp = onLaunchApp,
            onLongPress = onLongPress,
            modifier = Modifier.fadeBehindDrawer(drawerMotion),
        )
    }
    HomeRail(rail, drawer.sections, drawer.railAtStart, bottomClearance = HomeButtonRoom) {
        haptics.confirm()
        drawerMotion.openInPlace()
        onOpenDrawer()
    }
    // Last, so it is on top: a finger landing on the edge is the strip's, whatever shows there.
    RailScrubStrip(rail.scrub, enabled, drawer.railAtStart, bottomClearance = HomeButtonRoom)
}
