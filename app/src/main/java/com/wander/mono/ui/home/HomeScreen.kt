package com.wander.mono.ui.home

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchSource
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.calendar.CalendarEvent
import com.wander.mono.core.widgets.MonoWidgetHostView
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.ReadableLayout
import com.wander.mono.ui.components.readableColumn
import com.wander.mono.ui.edit.EditToolbarClearance
import com.wander.mono.ui.edit.HomeEditing
import com.wander.mono.ui.onboarding.SwipeHint
import com.wander.mono.ui.theme.LocalHighContrast

/**
 * The home screen over the wallpaper: the glance header at the top (or, [shell] reachable, just
 * over the dock) with the player under the clock ([nowPlaying], told whether the header is
 * compact), the rows where the user placed them ([HomeLook.listPosition], by default the bottom so
 * favourites sit in the thumb zone), and the search dock under them. [hero] is how far the hero
 * pull has grown the clock. Drags anywhere (past the end of the list when it scrolls) go to
 * [gestureScroll]: up pulls the drawer, down opens the notification shade. Long-pressing an empty
 * spot opens the home options and double-tapping one runs [onDoubleTap]; rows consume their own
 * presses, so they never reach them.
 * Long-pressing the clock opens its own sheet ([onClockOptions]); the player handles its own.
 * The favourite [pulse] keys, just added, pulses once, then [onPulsed] clears it.
 * The [lifted] favourites go first while headphones are connected, outside edit mode only.
 *
 * The scrim and the edit dim are not drawn here but by [HomeScrimLayer] under home, at the
 * [scrimEdges] measured here; [contentModifier] applies to the header, list and dock alone.
 */
@Composable
fun HomeScreen(
    rows: List<HomeRow>?,
    lifted: Set<AppKey>,
    glance: Glance,
    look: HomeLook,
    listState: LazyListState,
    icons: AppIcons,
    editing: HomeEditing,
    pulse: AppKey?,
    onPulsed: (AppKey) -> Unit,
    onLaunch: (LaunchableApp, LaunchSource?) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    onAppInfo: (LaunchableApp) -> Unit,
    onGrantAccess: () -> Unit,
    onOpenEvent: (CalendarEvent) -> Unit,
    onOpenDrawer: () -> Unit,
    onSearch: () -> Unit,
    gestureScroll: NestedScrollConnection,
    onDoubleTap: (() -> Unit)?,
    onHomeOptions: () -> Unit,
    onClockOptions: () -> Unit,
    createWidgetView: (Context, Int) -> MonoWidgetHostView,
    onWidgetLongPress: (WidgetHomeRow) -> Unit,
    scrimEdges: ScrimEdges,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    shell: HomeShell = HomeShell(),
    hero: () -> Float = { 0f },
    nowPlaying: @Composable (compact: Boolean) -> Unit = {},
    previews: NotificationPreviewViewModel = viewModel(),
) {
    // The header and search dock are not scrollable; this lets drags on them reach gestureScroll too.
    val stillDrag = rememberScrollableState { 0f }
    val motion = MaterialTheme.motionScheme
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val replyOpen = previews.replying.collectAsStateWithLifecycle().value != null
    val editPadding = animateDpAsState(if (editing.active) EditToolbarClearance else 16.dp, motion.defaultSpatialSpec())
    // The halo steps aside with the scrim while the window is unfocused.
    val shadow = wallpaperTextShadow(look.textShadow, halo = windowFocusStrength().value)

    // Without onSurface as the content colour, the clock and favourites would fall back to black.
    CompositionLocalProvider(
        LocalContentColor provides scheme.onSurface,
        LocalWallpaperTextShadow provides shadow,
        LocalHighContrast provides look.highContrast,
        LocalRowDensity provides look.density,
    ) {
        BoxWithConstraints(
            modifier
                .fillMaxSize()
                .pointerInput(onHomeOptions, onDoubleTap) {
                    detectTapGestures(
                        onDoubleTap = if (onDoubleTap != null) { _ -> onDoubleTap() } else null,
                        onLongPress = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onHomeOptions()
                        },
                    )
                }
                .nestedScroll(gestureScroll)
                .scrollable(stillDrag, Orientation.Vertical),
            contentAlignment = Alignment.TopCenter,
        ) {
            val gutter = ReadableLayout.gutter(maxWidth)
            // A phone on its side: the header shrinks so the list keeps room to scroll.
            val compactHeader = ReadableLayout.isCompactHeight(maxHeight)
            Column(
                Modifier
                    .then(contentModifier)
                    .onGloballyPositioned(scrimEdges::onFrame)
                    .fillMaxSize()
                    // Not the IME: home stays put behind the drawer while its search keyboard is up.
                    .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
                    .then(replyImePadding(replyOpen))
                    .readableColumn()
                    .padding(horizontal = gutter),
            ) {
                val header = @Composable {
                    HomeGlanceSlot(
                        glance,
                        shell,
                        compactHeader,
                        hero,
                        scrimEdges::onHeader,
                        onHomeOptions,
                        onClockOptions,
                    ) { nowPlaying(compactHeader) }
                }
                if (!shell.reachable) header()
                val listModifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onGloballyPositioned(scrimEdges::onList)
                ScaledLabels(shell.labelScale) {
                    HomeList(
                        rows = rows,
                        lifted = lifted,
                        listState = listState,
                        icons = icons,
                        editing = editing,
                        previews = previews,
                        pulse = pulse,
                        onPulsed = onPulsed,
                        // In edit mode the floating toolbar covers the list's foot: keep the last row clear of it.
                        contentPadding = PaddingValues(bottom = editPadding.value.coerceAtLeast(0.dp)),
                        position = look.listPosition,
                        onLaunch = onLaunch,
                        onLongPress = onLongPress,
                        onAppInfo = onAppInfo,
                        onGrantAccess = onGrantAccess,
                        onOpenEvent = onOpenEvent,
                        onOpenDrawer = onOpenDrawer,
                        createWidgetView = createWidgetView,
                        onWidgetLongPress = onWidgetLongPress,
                        modifier = listModifier,
                    )
                }
                if (shell.reachable) header()
                // Once, until the drawer is first opened; the empty state already says the same.
                SwipeHint(
                    hidden = editing.active || rows.orEmpty().none { it is HomeRow.App || it is HomeRow.Folder },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                // In edit mode the floating toolbar takes this place; the dock folds away as the
                // list's clearance grows on the same spec, so the rows never jump.
                AnimatedVisibility(
                    visible = !editing.active,
                    enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
                    exit = shrinkVertically(motion.defaultSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
                ) {
                    SearchDock(onSearch, Modifier.padding(top = 8.dp, bottom = 8.dp))
                }
            }
        }
    }
}
