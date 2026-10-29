package com.wander.mono.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.ui.drawer.DrawerScreen
import com.wander.mono.ui.drawer.DrawerViewModel
import com.wander.mono.ui.home.HomeScreen
import com.wander.mono.ui.home.HomeViewModel
import com.wander.mono.ui.sheet.AppSheet
import com.wander.mono.ui.sheet.AppSheetViewModel

/** Home, with the drawer sliding over it, the long-press sheet and the error snackbar. */
@Composable
fun LauncherApp(
    launcher: LauncherViewModel = viewModel(),
    home: HomeViewModel = viewModel(),
    drawer: DrawerViewModel = viewModel(),
    sheet: AppSheetViewModel = viewModel(),
) {
    val drawerOpen by launcher.drawerOpen.collectAsStateWithLifecycle()
    val drawerAnimates by launcher.drawerAnimates.collectAsStateWithLifecycle()
    val rows by home.rows.collectAsStateWithLifecycle()
    val glance by home.glance.collectAsStateWithLifecycle()
    val query by drawer.query.collectAsStateWithLifecycle()
    val drawerState by drawer.state.collectAsStateWithLifecycle()
    val sheetState by sheet.state.collectAsStateWithLifecycle()
    val homeList = rememberLazyListState()
    val snackbar = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val motion = MaterialTheme.motionScheme

    LaunchedEffect(launcher) { launcher.messages.collect { snackbar.showSnackbar(resources.getString(it)) } }
    LaunchedEffect(launcher) { launcher.scrollToTop.collect { homeList.animateScrollToItem(0) } }
    // Every way of closing the drawer (back, swipe, launch, home press) starts the next search afresh.
    LaunchedEffect(drawerOpen) { if (!drawerOpen) drawer.setQuery("") }
    // Back on home has nothing to go back to; it only ever closes what is open.
    BackHandler {
        when {
            drawerOpen && query.isNotEmpty() -> drawer.setQuery("")
            drawerOpen -> launcher.closeDrawer()
        }
    }

    Box {
        HomeScreen(
            rows = rows,
            glance = glance,
            listState = homeList,
            onLaunch = home::launch,
            onLongPress = { sheet.show(it.key) },
            onTogglePlay = home::togglePlayPause,
            onSkipNext = home::skipNext,
            onGrantAccess = home::openListenerSettings,
            onOpenDrawer = launcher::openDrawer,
        )
        AnimatedVisibility(
            visible = drawerOpen,
            enter = slideInVertically(motion.defaultSpatialSpec()) { it / SLIDE_FRACTION } +
                fadeIn(motion.defaultEffectsSpec()),
            exit = if (drawerAnimates) {
                slideOutVertically(motion.fastSpatialSpec()) { it / SLIDE_FRACTION } + fadeOut(motion.fastEffectsSpec())
            } else {
                ExitTransition.None
            },
        ) {
            DrawerScreen(
                query = query,
                state = drawerState,
                loadIcon = drawer::icon,
                onQueryChange = drawer::setQuery,
                onSubmit = { if (drawer.launchTopResult()) launcher.closeDrawer() },
                onLaunchApp = { if (drawer.launch(it)) launcher.closeDrawer() },
                onLaunchHit = { if (drawer.launch(it)) launcher.closeDrawer() },
                onLongPress = { sheet.show(it.key) },
                onClose = launcher::closeDrawer,
            )
        }
        SnackbarHost(
            snackbar,
            Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
        )
    }

    sheetState?.let { state ->
        AppSheet(
            state = state,
            onDismiss = sheet::dismiss,
            onShortcut = sheet::launchShortcut,
            onAddToHome = sheet::addToHome,
            onRemoveFromHome = sheet::removeFromHome,
            onMove = sheet::move,
            onSetHidden = sheet::setHidden,
            onAppInfo = sheet::openAppInfo,
            onUninstall = sheet::uninstall,
        )
    }
}

/** The drawer slides in from a quarter of the screen below, not from the very bottom. */
private const val SLIDE_FRACTION = 4
