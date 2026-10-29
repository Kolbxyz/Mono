package com.wander.mono.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.R
import com.wander.mono.ui.components.rememberCredentialPrompt
import com.wander.mono.ui.drawer.DrawerScreen
import com.wander.mono.ui.drawer.SearchHit
import com.wander.mono.ui.edit.EditEffects
import com.wander.mono.ui.edit.EditHomeViewModel
import com.wander.mono.ui.edit.EditToolbar
import com.wander.mono.ui.edit.rememberHomeEditing
import com.wander.mono.ui.drawer.DrawerViewModel
import com.wander.mono.ui.drawer.HitOutcome
import com.wander.mono.ui.home.HomeLookViewModel
import com.wander.mono.ui.home.HomeScreen
import com.wander.mono.ui.home.HomeViewModel
import com.wander.mono.ui.quiet.QuietDrawerGate
import com.wander.mono.ui.quiet.QuietViewModel
import com.wander.mono.ui.sheet.AppSheet
import com.wander.mono.ui.settings.SettingsScreen
import com.wander.mono.ui.sheet.AppSheetViewModel
import com.wander.mono.ui.sheet.HomeSheet
import com.wander.mono.ui.sheet.RenameDialog
import com.wander.mono.ui.sheet.RenameViewModel
import com.wander.mono.ui.theme.LocalReducedMotion
import com.wander.mono.ui.widgets.WidgetSheets
import com.wander.mono.ui.widgets.WidgetsViewModel

/** Home, with the drawer and settings sliding over it, the long-press sheets and the error snackbar. */
@Composable
fun LauncherApp(
    launcher: LauncherViewModel = viewModel(),
    home: HomeViewModel = viewModel(),
    homeLookModel: HomeLookViewModel = viewModel(),
    drawer: DrawerViewModel = viewModel(),
    sheet: AppSheetViewModel = viewModel(),
    edit: EditHomeViewModel = viewModel(),
    rename: RenameViewModel = viewModel(),
    quiet: QuietViewModel = viewModel(),
    widgets: WidgetsViewModel = viewModel(),
) {
    val drawerOpen by launcher.drawerOpen.collectAsStateWithLifecycle()
    val settingsOpen by launcher.settingsOpen.collectAsStateWithLifecycle()
    val homeOptionsOpen by launcher.homeOptionsOpen.collectAsStateWithLifecycle()
    val overlaysAnimate by launcher.overlaysAnimate.collectAsStateWithLifecycle()
    val rows by home.rows.collectAsStateWithLifecycle()
    val lifted by home.lifted.collectAsStateWithLifecycle()
    val glance by home.glance.collectAsStateWithLifecycle()
    val homeLook by homeLookModel.look.collectAsStateWithLifecycle()
    val query by drawer.query.collectAsStateWithLifecycle()
    val drawerState by drawer.state.collectAsStateWithLifecycle()
    val sheetState by sheet.state.collectAsStateWithLifecycle()
    val editingOn by launcher.editing.collectAsStateWithLifecycle()
    val pickingApp by launcher.pickingApp.collectAsStateWithLifecycle()
    val sheetUsage by sheet.usageToday.collectAsStateWithLifecycle()
    val quietOn by quiet.enabled.collectAsStateWithLifecycle()
    val unlockHidden = rememberCredentialPrompt(
        R.string.hidden_unlock_title,
        onSuccess = drawer::unlockHidden,
        onNoScreenLock = drawer::reportNoScreenLock,
    )
    val homeList = rememberLazyListState()
    val editing = rememberHomeEditing(editingOn, homeList, edit, { launcher.setEditing(true) }, rename::show)
    val pick: (LaunchableApp) -> Unit = { app ->
        edit.add(app.key)
        launcher.closeDrawer()
    }
    val snackbar = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val motion = MaterialTheme.motionScheme

    val afterHit: (HitOutcome) -> Unit = { outcome ->
        when (outcome) {
            HitOutcome.Opened -> launcher.closeDrawer()
            HitOutcome.OpenMonoSettings -> launcher.openSettings()
            HitOutcome.Stayed -> Unit
        }
    }

    LaunchedEffect(launcher) { launcher.messages.collect { snackbar.showSnackbar(resources.getString(it)) } }
    val reducedMotion = LocalReducedMotion.current
    LaunchedEffect(launcher, reducedMotion) {
        launcher.scrollToTop.collect {
            if (reducedMotion) homeList.scrollToItem(0) else homeList.animateScrollToItem(0)
        }
    }
    // Every way of closing the drawer (back, swipe, launch, home press) starts the next search afresh.
    LaunchedEffect(drawerOpen) { if (!drawerOpen) drawer.setQuery("") }
    EditEffects(editing, rows, homeList)
    // Back on home has nothing to go back to; it only ever closes what is open.
    BackHandler {
        when {
            settingsOpen -> launcher.closeSettings()
            drawerOpen && query.isNotEmpty() -> drawer.setQuery("")
            drawerOpen -> launcher.closeDrawer()
            editingOn -> launcher.setEditing(false)
        }
    }

    Box {
        HomeScreen(
            rows = rows,
            lifted = lifted,
            glance = glance,
            look = homeLook,
            listState = homeList,
            editing = editing,
            onLaunch = home::launch,
            onLongPress = { sheet.show(it.key) },
            onAppInfo = { sheet.openAppInfo(it.key) },
            onTogglePlay = home::togglePlayPause,
            onSkipNext = home::skipNext,
            onSkipPrevious = home::skipPrevious,
            onSeek = home::seekTo,
            onPlayQueueItem = home::playQueueItem,
            onOpenOutput = home::openOutputSwitcher,
            onGrantAccess = home::openListenerSettings,
            onOpenEvent = home::openEvent,
            onOpenDrawer = launcher::openDrawer,
            onHomeOptions = { launcher.showHomeOptions(true) },
            createWidgetView = widgets::createView,
            onWidgetLongPress = { widgets.showOptions(it.widget.placement.appWidgetId) },
        )
        AnimatedVisibility(
            visible = drawerOpen,
            enter = slideInVertically(motion.defaultSpatialSpec()) { it / SLIDE_FRACTION } +
                fadeIn(motion.defaultEffectsSpec()),
            exit = if (overlaysAnimate) {
                slideOutVertically(motion.fastSpatialSpec()) { it / SLIDE_FRACTION } + fadeOut(motion.fastEffectsSpec())
            } else {
                ExitTransition.None
            },
        ) {
            // Hidden apps unlocked for this opening only.
            DisposableEffect(Unit) { onDispose(drawer::relockHidden) }
            QuietDrawerGate(quiet = quietOn, onClose = launcher::closeDrawer) {
                DrawerScreen(
                    query = query,
                    state = drawerState,
                    loadIcon = drawer::icon,
                    onQueryChange = drawer::setQuery,
                    onSubmit = { afterHit(drawer.launchTopResult()) },
                    onLaunchApp = { if (pickingApp) pick(it) else if (drawer.launch(it)) launcher.closeDrawer() },
                    onLaunchHit = { hit ->
                        if (pickingApp && hit is SearchHit.App) pick(hit.entry.app) else afterHit(drawer.launch(hit))
                    },
                    onLongPress = { sheet.show(it.key) },
                    onClose = launcher::closeDrawer,
                    onOpenSettings = launcher::openSettings,
                    onUnlockHidden = unlockHidden,
                    pickingApp = pickingApp,
                )
            }
        }
        // Over the drawer when opened from it, so back from settings returns to the drawer.
        AnimatedVisibility(
            visible = settingsOpen,
            enter = slideInHorizontally(motion.defaultSpatialSpec()) { it / SLIDE_FRACTION } +
                fadeIn(motion.defaultEffectsSpec()),
            exit = if (overlaysAnimate) {
                slideOutHorizontally(motion.fastSpatialSpec()) { it / SLIDE_FRACTION } +
                    fadeOut(motion.fastEffectsSpec())
            } else {
                ExitTransition.None
            },
        ) {
            SettingsScreen(onBack = launcher::closeSettings)
        }
        EditToolbar(
            visible = editingOn && !drawerOpen && !settingsOpen,
            onDone = { launcher.setEditing(false) },
            onAddApp = launcher::pickApp,
            onOpenSettings = launcher::openSettings,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
        SnackbarHost(
            snackbar,
            Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
        )
    }

    if (homeOptionsOpen) {
        HomeSheet(
            quiet = quietOn,
            onDismiss = { launcher.showHomeOptions(false) },
            onSetQuiet = {
                launcher.showHomeOptions(false)
                quiet.setEnabled(it)
            },
            onOpenSettings = launcher::openSettings,
            onAddWidget = {
                launcher.showHomeOptions(false)
                widgets.openPicker()
            },
            onEditHome = { launcher.setEditing(true) },
        )
    }
    WidgetSheets(widgets)
    sheetState?.let { state ->
        AppSheet(
            state = state,
            usageToday = sheetUsage,
            onDismiss = sheet::dismiss,
            onShortcut = sheet::launchShortcut,
            onAddToHome = sheet::addToHome,
            onRemoveFromHome = sheet::removeFromHome,
            onMove = sheet::move,
            onSetHidden = sheet::setHidden,
            onSetLiftsWithHeadphones = sheet::setLiftsWithHeadphones,
            onRename = {
                sheet.dismiss()
                rename.show(it)
            },
            onAppInfo = sheet::openAppInfo,
            onUninstall = sheet::uninstall,
        )
    }
    RenameDialog(rename)
}

/** Overlays slide in from a quarter of the screen away (below, or to the side), not from the edge. */
private const val SLIDE_FRACTION = 4
