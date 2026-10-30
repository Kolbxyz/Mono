package com.wander.mono.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.wander.mono.ui.home.HomePlayerSlot
import com.wander.mono.ui.home.HomeScreen
import com.wander.mono.ui.home.HomeScrimLayer
import com.wander.mono.ui.home.HomeShellViewModel
import com.wander.mono.ui.home.HomeViewModel
import com.wander.mono.ui.home.ScrimEdges
import com.wander.mono.ui.home.media.collapsesPlayerOutside
import com.wander.mono.ui.home.rememberHomePlayerExpansion
import com.wander.mono.ui.sheet.AppSheetViewModel
import com.wander.mono.ui.widgets.WidgetsViewModel

/**
 * Home wired to the launcher, the same over the whole window or as the wide layout's left pane.
 * [covered] is true while the drawer or settings hides it, so a just-added favourite pulses only
 * once home is in view again, and the player under the clock goes back to how it starts.
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
    scrimEdges: ScrimEdges,
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
    val player by home.player.collectAsStateWithLifecycle()
    val expansion = rememberHomePlayerExpansion(listState, covered)
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
        onGrantAccess = home::openListenerSettings,
        onOpenEvent = home::openEvent,
        onOpenDrawer = { launcher.openDrawer() },
        onSearch = { launcher.openDrawer(focusSearch = true) },
        gestureScroll = gestureScroll,
        onDoubleTap = if (doubleTapLock) gestures::lockScreen else null,
        onHomeOptions = { launcher.showHomeOptions(true) },
        onClockOptions = { launcher.showClockSheet(true) },
        createWidgetView = widgets::createView,
        onWidgetLongPress = { widgets.showOptions(it.widget.placement.appWidgetId) },
        scrimEdges = scrimEdges,
        // A press anywhere on home outside the expanded player collapses it.
        modifier = modifier.collapsesPlayerOutside(expansion),
        contentModifier = contentModifier,
        shell = shell,
        hero = heroProgress,
        nowPlaying = { compact ->
            HomePlayerSlot(player, expansion, home, icons, compact, onOptions = { launcher.showMediaSheet(true) })
        },
    )
}

/**
 * Home's scrim and edit dim ([HomeScrimLayer]) over the whole window, under home and every layer
 * that scales it; on a [wide] window only under home's pane, the drawer's beside it drawing its own.
 */
@Composable
fun LauncherHomeScrim(
    homeLookModel: HomeLookViewModel,
    shellModel: HomeShellViewModel,
    editing: Boolean,
    edges: ScrimEdges,
    listState: LazyListState,
    wide: Boolean,
    modifier: Modifier = Modifier,
) {
    val look by homeLookModel.look.collectAsStateWithLifecycle()
    val shell by shellModel.shell.collectAsStateWithLifecycle()
    Row(modifier.fillMaxSize()) {
        HomeScrimLayer(look, shell, editing, edges, listState, Modifier.weight(1f))
        if (wide) Spacer(Modifier.weight(1f))
    }
}
