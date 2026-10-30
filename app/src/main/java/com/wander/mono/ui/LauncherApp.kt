package com.wander.mono.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.settings.SwipeUpAction
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.rememberCredentialPrompt
import com.wander.mono.ui.components.rememberPullUpConnection
import com.wander.mono.ui.drawer.DrawerViewModel
import com.wander.mono.ui.edit.EditEffects
import com.wander.mono.ui.edit.EditHomeViewModel
import com.wander.mono.ui.edit.EditToolbar
import com.wander.mono.ui.edit.rememberHomeEditing
import com.wander.mono.ui.home.HomeLookViewModel
import com.wander.mono.ui.home.HomeShellViewModel
import com.wander.mono.ui.home.HomeViewModel
import com.wander.mono.ui.home.rememberHeroConnection
import com.wander.mono.ui.home.rememberHeroPull
import com.wander.mono.ui.quiet.QuietViewModel
import com.wander.mono.ui.settings.SettingsScreen
import com.wander.mono.ui.settings.SettingsTwoPane
import com.wander.mono.ui.sheet.AppSheetViewModel
import com.wander.mono.ui.sheet.RenameViewModel
import com.wander.mono.ui.theme.LocalReducedMotion
import com.wander.mono.ui.widgets.WidgetsViewModel

/**
 * Home, with the drawer and settings sliding over it, the long-press sheets and the error snackbar.
 * On a wide window ([rememberWideWindow]) the drawer stands beside home instead, and settings
 * opens as a two-pane hub. Every overlay recedes what is under it the same way ([recede]).
 */
@Composable
fun LauncherApp(
    launcher: LauncherViewModel = viewModel(),
    home: HomeViewModel = viewModel(),
    homeLookModel: HomeLookViewModel = viewModel(),
    shellModel: HomeShellViewModel = viewModel(),
    drawer: DrawerViewModel = viewModel(),
    sheet: AppSheetViewModel = viewModel(),
    edit: EditHomeViewModel = viewModel(),
    rename: RenameViewModel = viewModel(),
    quiet: QuietViewModel = viewModel(),
    widgets: WidgetsViewModel = viewModel(),
    gestures: GestureViewModel = viewModel(),
) {
    val drawerOpen by launcher.drawerOpen.collectAsStateWithLifecycle()
    val swipeUp by gestures.swipeUp.collectAsStateWithLifecycle()
    val swipeDown by gestures.swipeDown.collectAsStateWithLifecycle()
    val settingsOpen by launcher.settingsOpen.collectAsStateWithLifecycle()
    val homeOptionsOpen by launcher.homeOptionsOpen.collectAsStateWithLifecycle()
    val sheetOpen = sheet.state.collectAsStateWithLifecycle().value != null || homeOptionsOpen
    val overlaysAnimate by launcher.overlaysAnimate.collectAsStateWithLifecycle()
    val rows by home.rows.collectAsStateWithLifecycle()
    val query by drawer.query.collectAsStateWithLifecycle()
    val drawerState by drawer.state.collectAsStateWithLifecycle()
    val editingOn by launcher.editing.collectAsStateWithLifecycle()
    val quietOn by quiet.enabled.collectAsStateWithLifecycle()
    val wide = rememberWideWindow()
    val unlockHidden = rememberCredentialPrompt(
        R.string.hidden_unlock_title,
        onSuccess = drawer::unlockHidden,
        onNoScreenLock = drawer::reportNoScreenLock,
    )
    val homeList = rememberLazyListState()
    val homeIcons = remember(drawerState.iconMode, drawerState.iconPack) {
        AppIcons(drawerState.iconMode, drawer::icon, drawerState.iconPack)
    }
    val editing = rememberHomeEditing(editingOn, homeList, edit, { launcher.setEditing(true) }, rename::show)
    val drawerMotion = rememberDrawerMotion(drawerOpen) { open ->
        if (open) launcher.openDrawer(focusSearch = swipeUp == SwipeUpAction.Search) else launcher.closeDrawer()
    }
    // Wide, the drawer has nowhere to rise: swiping up brings up the side panel's search instead.
    val panelPull = rememberPanelSearchPull { launcher.openDrawer(focusSearch = true) }
    val pullDown = gestures.pullDownFor(swipeDown) { launcher.openDrawer(focusSearch = true) }
    val hero = rememberHeroPull(pullDownActive = pullDown != null)
    val homeGestures = rememberHeroConnection(
        rememberPullUpConnection(if (wide) panelPull else drawerMotion, onPullDown = pullDown),
        hero,
    )
    val settings = rememberOverlayPresence(settingsOpen)
    val motion = MaterialTheme.motionScheme
    val sheetCover by animateFloatAsState(
        if (sheetOpen) 1f else 0f,
        if (sheetOpen) motion.defaultSpatialSpec() else motion.fastSpatialSpec(),
    )

    val reducedMotion = LocalReducedMotion.current
    LaunchedEffect(launcher, reducedMotion) {
        launcher.scrollToTop.collect {
            if (reducedMotion) homeList.scrollToItem(0) else homeList.animateScrollToItem(0)
        }
    }
    // Taps, launches, home presses and back move the drawer here; drags and flings move it themselves.
    LaunchedEffect(drawerOpen) { drawerMotion.follow(drawerOpen, overlaysAnimate) }
    // The side panel never leaves, so closing it is where the next opening starts afresh.
    LaunchedEffect(wide, drawerOpen) {
        if (wide && !drawerOpen) {
            drawer.setQuery("")
            drawer.relockHidden()
            drawer.clearListFilters()
            launcher.drawerClosed()
        }
    }
    EditEffects(editing, rows, homeList)
    // Back on home has nothing to go back to; it only ever closes what is open. Settings, the drawer
    // and the sheets take back first with their own predictive handlers.
    BackHandler {
        when {
            query.isNotEmpty() && (drawerOpen || wide) -> drawer.setQuery("")
            drawerOpen -> launcher.closeDrawer()
            editingOn -> launcher.setEditing(false)
        }
    }

    val homeLayer = @Composable { modifier: Modifier, contentModifier: Modifier ->
        LauncherHome(
            launcher, home, homeLookModel, shellModel, sheet, widgets, gestures,
            listState = homeList,
            icons = homeIcons,
            editing = editing,
            covered = (drawerOpen && !wide) || settingsOpen,
            gestureScroll = homeGestures,
            hero = hero,
            modifier = modifier,
            contentModifier = contentModifier,
        )
    }
    Box(Modifier.fillMaxSize().onSizeChanged { drawerMotion.travel = it.height * TRAVEL_FRACTION }) {
        Box(Modifier.fillMaxSize().recede({ sheetCover }, OverlayLevel.Sheet)) {
            Box(Modifier.fillMaxSize().recede(settings::cover, OverlayLevel.Screen, overlayScrim())) {
                if (wide) {
                    Row(Modifier.fillMaxSize()) {
                        homeLayer(Modifier.weight(1f), Modifier)
                        // Never blurs the wallpaper: it is home's too, beside the panel.
                        Box(Modifier.weight(1f)) {
                            LauncherDrawer(
                                drawerOpen, { 0f }, quietOn, launcher, drawer, sheet, edit, unlockHidden,
                            )
                        }
                    }
                } else {
                    homeLayer(Modifier.fadeBehindDrawer(drawerMotion), Modifier.shrinkBehindDrawer(drawerMotion))
                    DrawerLayer(
                        motion = drawerMotion,
                        open = drawerOpen,
                        // Settings over the drawer and a typed query each take back first.
                        backEnabled = !settingsOpen && query.isEmpty(),
                        onBack = launcher::closeDrawer,
                        // Every way of closing (back, swipe, launch, home press) starts the next opening
                        // afresh, once the drawer has faded out showing what it showed.
                        onClosed = {
                            drawer.setQuery("")
                            drawer.clearListFilters()
                            launcher.drawerClosed()
                        },
                    ) {
                        // Hidden apps unlocked for this opening only.
                        DisposableEffect(Unit) { onDispose(drawer::relockHidden) }
                        LauncherDrawer(
                            drawerOpen, { drawerMotion.shown }, quietOn, launcher, drawer, sheet, edit, unlockHidden,
                        )
                    }
                }
            }
            SettingsLayer(settings, settingsOpen, animateExit = overlaysAnimate, onBack = launcher::closeSettings) {
                if (wide) {
                    SettingsTwoPane(onBack = launcher::closeSettings)
                } else {
                    SettingsScreen(onBack = launcher::closeSettings)
                }
            }
        }
        EditToolbar(
            visible = editingOn && (!drawerOpen || wide) && !settingsOpen,
            onDone = { launcher.setEditing(false) },
            onAddApp = launcher::pickApp,
            onAddWidget = widgets::openPicker,
            onOpenSettings = launcher::openSettings,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
        LauncherSnackbar(
            launcher,
            overlayOpen = (drawerOpen && !wide) || settingsOpen,
            editing = editingOn,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
    LauncherSheets(launcher, sheet, rename, quiet, widgets, homeIcons)
    AppLockGate()
}
