package com.wander.mono.ui.home

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
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
import com.wander.mono.ui.theme.LocalHighContrast

/**
 * The home screen over the wallpaper: the glance header at the top, the rows anchored to the
 * bottom so favourites sit in the thumb zone, and the search dock under them. Drags anywhere
 * (past the end of the list when it scrolls) go to [gestureScroll]: up pulls the drawer, down
 * opens the notification shade. Long-pressing the clock or an empty spot opens the home options and
 * double-tapping one runs [onDoubleTap]; rows consume their own presses, so they never reach them.
 * The favourite [pulse] keys, just added, pulses once, then [onPulsed] clears it.
 * The [lifted] favourites go first while headphones are connected, outside edit mode only.
 *
 * The scrim and the edit dim are drawn by the outer layer ([modifier]); [contentModifier] applies
 * to the header, list and dock alone, so scaling them (behind the drawer) leaves the scrim whole.
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
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (positionMs: Long) -> Unit,
    onPlayQueueItem: (id: Long) -> Unit,
    onOpenOutput: () -> Unit,
    onGrantAccess: () -> Unit,
    onOpenEvent: (CalendarEvent) -> Unit,
    onOpenDrawer: () -> Unit,
    onSearch: () -> Unit,
    gestureScroll: NestedScrollConnection,
    onDoubleTap: (() -> Unit)?,
    onHomeOptions: () -> Unit,
    createWidgetView: (Context, Int) -> MonoWidgetHostView,
    onWidgetLongPress: (WidgetHomeRow) -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    previews: NotificationPreviewViewModel = viewModel(),
) {
    // The header and search dock are not scrollable; this lets drags on them reach gestureScroll too.
    val stillDrag = rememberScrollableState { 0f }
    val motion = MaterialTheme.motionScheme
    val scheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val scrimEdges = remember { ScrimEdges() }
    val replyOpen = previews.replying.collectAsStateWithLifecycle().value != null
    val legibleAlpha = remember(look.tone, scheme.surface, scheme.onSurface) {
        scrimAlpha(look.tone, scheme.surface.toArgb(), scheme.onSurface.toArgb())
    }
    // Edit mode dims the wallpaper (spec 4.6).
    val editDim = animateFloatAsState(if (editing.active) EDIT_DIM else 0f, motion.defaultEffectsSpec())
    val editPadding = animateDpAsState(if (editing.active) EditToolbarClearance else 16.dp, motion.defaultSpatialSpec())
    val scrimAlpha = homeScrimAlpha(legibleAlpha, look.textShadow, look.highContrast)
    val shadow = wallpaperTextShadow(look.textShadow)

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
                .homeScrim(
                    color = scheme.surface.copy(alpha = scrimAlpha),
                    headerBottom = { scrimEdges.headerBottom },
                    listTop = { scrimEdges.listTop + listState.contentTop() },
                    reach = with(density) { SCRIM_REACH.toPx() },
                )
                .drawBehind { drawRect(scheme.surface.copy(alpha = editDim.value)) }
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
                val optionsLabel = stringResource(R.string.action_home_options)
                GlanceHeader(
                    glance,
                    compact = compactHeader,
                    modifier = Modifier
                        .onGloballyPositioned(scrimEdges::onHeader)
                        .semantics {
                            onLongClick(optionsLabel) {
                                onHomeOptions()
                                true
                            }
                        },
                )
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
                    contentPadding = PaddingValues(bottom = editPadding.value),
                    onLaunch = onLaunch,
                    onLongPress = onLongPress,
                    onAppInfo = onAppInfo,
                    onTogglePlay = onTogglePlay,
                    onSkipNext = onSkipNext,
                    onSkipPrevious = onSkipPrevious,
                    onSeek = onSeek,
                    onPlayQueueItem = onPlayQueueItem,
                    onOpenOutput = onOpenOutput,
                    onGrantAccess = onGrantAccess,
                    onOpenEvent = onOpenEvent,
                    onOpenDrawer = onOpenDrawer,
                    createWidgetView = createWidgetView,
                    onWidgetLongPress = onWidgetLongPress,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .onGloballyPositioned(scrimEdges::onList),
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

/** Where the first row starts inside the list viewport: 0 once the list is scrolled or full. */
private fun LazyListState.contentTop(): Float {
    val first = layoutInfo.visibleItemsInfo.firstOrNull() ?: return 0f
    return if (first.index == 0) (first.offset - layoutInfo.viewportStartOffset).toFloat().coerceAtLeast(0f) else 0f
}

/** How much edit mode dims the wallpaper, with the surface colour (spec 4.6). */
private const val EDIT_DIM = 0.6f

/** How far the scrim eases out into the empty space past the header and before the list. */
private val SCRIM_REACH = 120.dp
