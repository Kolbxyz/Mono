package com.wander.mono.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.edit.HomeEditing
import com.wander.mono.ui.home.HeroPull
import com.wander.mono.ui.home.HomeLookViewModel
import com.wander.mono.ui.home.HomeScreen
import com.wander.mono.ui.home.HomeShellViewModel
import com.wander.mono.ui.home.HomeViewModel
import com.wander.mono.ui.sheet.AppSheetViewModel
import com.wander.mono.ui.widgets.WidgetsViewModel

/**
 * Home wired to the launcher, the same over the whole window or as the wide layout's left pane.
 * [covered] is true while the drawer or settings hides it, so a just-added favourite pulses only
 * once home is in view again.
 */
@Composable
fun LauncherHome(
    launcher: LauncherViewModel,
    home: HomeViewModel,
    homeLookModel: HomeLookViewModel,
    shellModel: HomeShellViewModel,
    sheet: AppSheetViewModel,
    widgets: WidgetsViewModel,
    gestures: GestureViewModel,
    listState: LazyListState,
    icons: AppIcons,
    editing: HomeEditing,
    covered: Boolean,
    gestureScroll: NestedScrollConnection,
    hero: HeroPull,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
) {
    val rows by home.rows.collectAsStateWithLifecycle()
    val lifted by home.lifted.collectAsStateWithLifecycle()
    val justAdded by home.justAdded.collectAsStateWithLifecycle()
    val glance by home.glance.collectAsStateWithLifecycle()
    val look by homeLookModel.look.collectAsStateWithLifecycle()
    val shell by shellModel.shell.collectAsStateWithLifecycle()
    val doubleTapLock by gestures.doubleTapLock.collectAsStateWithLifecycle()
    val heroProgress = remember(hero) { { hero.progress } }
    HomeScreen(
        rows = rows,
        lifted = lifted,
        glance = glance,
        look = look,
        listState = listState,
        icons = icons,
        editing = editing,
        // Pulses once home is in view again, not behind the drawer it was added from.
        pulse = justAdded.takeUnless { covered },
        onPulsed = home::pulsed,
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
        onOpenDrawer = { launcher.openDrawer() },
        onSearch = { launcher.openDrawer(focusSearch = true) },
        gestureScroll = gestureScroll,
        onDoubleTap = if (doubleTapLock) gestures::lockScreen else null,
        onHomeOptions = { launcher.showHomeOptions(true) },
        createWidgetView = widgets::createView,
        onWidgetLongPress = { widgets.showOptions(it.widget.placement.appWidgetId) },
        modifier = modifier,
        contentModifier = contentModifier,
        shell = shell,
        hero = heroProgress,
    )
}
