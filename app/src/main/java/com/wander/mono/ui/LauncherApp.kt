package com.wander.mono.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.onSizeChanged
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.settings.DrawerSort
import com.wander.mono.data.settings.SwipeUpAction
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.SheetBack
import com.wander.mono.ui.components.depthScrim
import com.wander.mono.ui.components.outOfFocus
import com.wander.mono.ui.components.rememberCredentialPrompt
import com.wander.mono.ui.components.rememberPullUpConnection
import com.wander.mono.ui.drawer.DrawerViewModel
import com.wander.mono.ui.edit.EditEffects
import com.wander.mono.ui.edit.EditHomeViewModel
import com.wander.mono.ui.edit.EditToolbar
import com.wander.mono.ui.edit.rememberHomeEditing
import com.wander.mono.ui.home.HomeLookViewModel
import com.wander.mono.ui.home.HomeRailState
import com.wander.mono.ui.home.HomeShellViewModel
import com.wander.mono.ui.home.HomeViewModel
import com.wander.mono.ui.home.ScrimEdges
import com.wander.mono.ui.home.givesWayToHeldLetter
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
    val sheetOpen = anySheetOpen(launcher, sheet, rename, widgets)
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
    val scrimEdges = remember { ScrimEdges() }
    val homeIcons = remember(drawerState.iconMode, drawerState.iconPack) {
        AppIcons(drawerState.iconMode, drawer::icon, drawer::cachedIcon, drawerState.iconPack)
    }
    val editing = rememberHomeEditing(
        editingOn,
        homeList,
        edit,
        onEnter = { launcher.setEditing(true) },
        onHoldCancelled = sheet::dismiss,
        onRename = rename::show,
    )
    val drawerMotion = rememberDrawerMotion(drawerOpen) { open ->
        if (open) launcher.openDrawer(focusSearch = swipeUp == SwipeUpAction.Search) else launcher.closeDrawer()
    }
    // Home's own rail, from a finger on its edge.
    val homeRail = remember { HomeRailState() }
    // Only while the drawer's list has its rail to land in: A–Z, with letters.
    val railOnList = drawerState.controls.choices.sort == DrawerSort.Alphabetical && drawerState.sections.isNotEmpty()
    // Wide, the drawer has nowhere to rise: swiping up brings up the side panel, with its keyboard
    // only when the swipe up is set to search.
    val panelPull = rememberPanelSearchPull { launcher.openDrawer(focusSearch = swipeUp == SwipeUpAction.Search) }
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
    val sheetBack = remember { SheetBack() }

    val reducedMotion = LocalReducedMotion.current
    LaunchedEffect(launcher, reducedMotion) {
        launcher.scrollToTop.collect {
            if (reducedMotion) homeList.scrollToItem(0) else homeList.animateScrollToItem(0)
        }
    }
    // Taps, launches, home presses and back move the drawer here; drags and flings move it themselves.
    LaunchedEffect(drawerOpen) { drawerMotion.follow(drawerOpen, overlaysAnimate) }
    SidePanelReset(wide, drawerOpen, launcher, drawer)
    EditEffects(editing, rows, homeList)
    LauncherBack(launcher, drawer, query, drawerOpen, editingOn, wide)

    val homeLayer = @Composable { modifier: Modifier, contentModifier: Modifier ->
        LauncherHome(
            launcher, home, homeLookModel, shellModel, sheet, widgets, gestures,
            listState = homeList,
            icons = homeIcons,
            editing = editing,
            covered = (drawerOpen && !wide) || settingsOpen,
            gestureScroll = homeGestures,
            hero = hero,
            scrimEdges = scrimEdges,
            modifier = modifier,
            contentModifier = contentModifier,
        )
    }
    // The blur is motion of its own: with motion reduced, what is under settings only dims.
    val settingsDim = depthScrim(overlayScrim(), blur = !reducedMotion)
    val sheetDepth = { coverOf(sheetCover, sheetBack.progress) }
    Box(Modifier.fillMaxSize().onSizeChanged { drawerMotion.travel = it.height * TRAVEL_FRACTION }) {
        // Scrims never scale with the layers they lie on, so no edge of theirs shows as those recede.
        LauncherHomeScrim(
            homeLookModel, shellModel, editing.active, scrimEdges, homeList, wide,
            if (wide) Modifier else Modifier.fadeBehindDrawer(drawerMotion),
        )
        Box(Modifier.fillMaxSize().recede(sheetDepth, OverlayLevel.Sheet)) {
            Box(
                Modifier
                    .fillMaxSize()
                    // Under the whole of settings, which is opaque, nothing here shows: no blur pass for it.
                    .drawWithContent { if (settings.cover < 1f) drawContent() }
                    .recede(settings::cover, OverlayLevel.Screen)
                    .outOfFocus(settings::cover, blur = !reducedMotion),
            ) {
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
                    homeLayer(
                        Modifier.fadeBehindDrawer(drawerMotion),
                        Modifier.shrinkBehindDrawer(drawerMotion).givesWayToHeldLetter(homeRail),
                    )
                    HomeRailLayer(
                        homeRail, drawerState, homeIcons,
                        enabled = !drawerOpen && !editingOn && !quietOn && railOnList,
                        drawerMotion = drawerMotion,
                        onOpenDrawer = { launcher.openDrawer() },
                        onLaunchApp = { drawer.launch(it) },
                        onLongPress = { sheet.show(it.key) },
                    )
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
                            landing = homeRail.landing,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.fillMaxSize().overlayDim(settings::cover, settingsDim))
        // Receding under a sheet as the layers under it do, in step.
        Box(Modifier.fillMaxSize().recede(sheetDepth, OverlayLevel.Sheet)) {
            SettingsLayer(settings, settingsOpen, animateExit = overlaysAnimate, onBack = launcher::closeSettings) {
                // Hidden apps unlocked in settings (its list, a backup) stay open only until it closes.
                DisposableEffect(Unit) { onDispose(drawer::relockHidden) }
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
            onAddApp = { launcher.pickApp(PickTarget.Favorite) },
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
    LauncherSheets(launcher, sheet, rename, quiet, widgets, homeIcons, sheetBack)
    AppLockGate()
}
