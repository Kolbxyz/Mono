package com.wander.mono.ui.home

import android.content.Context
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.calendar.CalendarEvent
import com.wander.mono.core.widgets.MonoWidgetHostView
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.ReadableLayout
import com.wander.mono.ui.components.readableColumn
import com.wander.mono.ui.edit.EditToolbarClearance
import com.wander.mono.ui.edit.HomeEditing
import com.wander.mono.ui.edit.ReorderableFavorite
import com.wander.mono.ui.edit.ReorderableWidget
import com.wander.mono.ui.theme.LocalHighContrast

/**
 * The home screen over the wallpaper: the glance header at the top, the rows anchored to the
 * bottom so favourites sit in the thumb zone, and the search dock under them. Drags anywhere
 * (past the end of the list when it scrolls) go to [gestureScroll]: up pulls the drawer, down
 * opens the notification shade. Long-pressing the clock or an empty spot opens the home options and
 * double-tapping one runs [onDoubleTap]; rows consume their own presses, so they never reach them.
 * The [lifted] favourites go first while headphones are connected, outside edit mode only.
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
    onLaunch: (LaunchableApp) -> Unit,
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
) {
    // The header and search dock are not scrollable; this lets drags on them reach gestureScroll too.
    val stillDrag = rememberScrollableState { 0f }
    val motion = MaterialTheme.motionScheme
    val scheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val headerBottom = remember { mutableFloatStateOf(0f) }
    val listViewportTop = remember { mutableFloatStateOf(0f) }
    val legibleAlpha = remember(look.tone, scheme.surface, scheme.onSurface) {
        scrimAlpha(look.tone, scheme.surface.toArgb(), scheme.onSurface.toArgb())
    }
    // Edit mode dims the wallpaper (spec 4.6).
    val editDim = animateFloatAsState(if (editing.active) EDIT_DIM else 0f, motion.defaultEffectsSpec())
    val editPadding = animateDpAsState(if (editing.active) EditToolbarClearance else 16.dp, motion.defaultSpatialSpec())
    val scrimAlpha = homeScrimAlpha(legibleAlpha, look.textShadow, look.highContrast)
    // The user's text shadow replaces the scrim; otherwise a soft halo in the scrim's colour
    // carries the text where the eased scrim has thinned out.
    val shadow = with(density) {
        if (look.textShadow) {
            Shadow(scheme.scrim.copy(alpha = SHADOW_ALPHA), Offset(0f, 1.dp.toPx()), blurRadius = 4.dp.toPx())
        } else {
            Shadow(scheme.surface.copy(alpha = HALO_ALPHA), Offset.Zero, blurRadius = 12.dp.toPx())
        }
    }

    // Without onSurface as the content colour, the clock and favourites would fall back to black.
    CompositionLocalProvider(
        LocalContentColor provides scheme.onSurface,
        LocalWallpaperTextShadow provides shadow,
        LocalHighContrast provides look.highContrast,
    ) {
        BoxWithConstraints(
            modifier
                .fillMaxSize()
                .homeScrim(
                    color = scheme.surface.copy(alpha = scrimAlpha),
                    headerBottom = { headerBottom.floatValue },
                    listTop = { listViewportTop.floatValue + listState.contentTop() },
                    reach = with(density) { SCRIM_REACH.toPx() },
                )
                .drawBehind { drawRect(scheme.surface.copy(alpha = editDim.value)) }
                .pointerInput(onHomeOptions, onDoubleTap) {
                    detectTapGestures(
                        onDoubleTap = if (onDoubleTap != null) { _ -> onDoubleTap() } else null,
                        onLongPress = { onHomeOptions() },
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
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .readableColumn()
                    .padding(horizontal = gutter),
            ) {
                val optionsLabel = stringResource(R.string.action_home_options)
                GlanceHeader(
                    glance,
                    compact = compactHeader,
                    modifier = Modifier
                        .onGloballyPositioned { headerBottom.floatValue = it.boundsInRoot().bottom }
                        .semantics {
                            onLongClick(optionsLabel) {
                                onHomeOptions()
                                true
                            }
                        },
                )
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .onGloballyPositioned { listViewportTop.floatValue = it.positionInRoot().y },
                    verticalArrangement = Arrangement.Bottom,
                    // In edit mode the floating toolbar covers the list's foot: keep the last row clear of it.
                    contentPadding = PaddingValues(bottom = editPadding.value),
                ) {
                    // The now-playing and prompt rows are not favourites: the empty state shows until there is one.
                    if (rows != null && !editing.active && rows.none { it is HomeRow.App }) {
                        item(key = "empty", contentType = "empty") {
                            EmptyHome(onChooseFavorites = onOpenDrawer, modifier = Modifier.animateItem())
                        }
                    }
                    val ordered = editing.shownRows(rows)
                    val movable = ordered.filter { it is HomeRow.App || it is WidgetHomeRow }.map { it.key }
                    // Saved order while editing; outside it, favourites chosen for headphones go first.
                    val shown = if (editing.showsSavedOrder) ordered else liftedFirst(ordered, lifted)
                    items(shown, key = { it.key }, contentType = { it::class }) { row ->
                        val animated = Modifier.animateItem(
                            fadeInSpec = motion.defaultEffectsSpec(),
                            placementSpec = motion.defaultSpatialSpec(),
                            fadeOutSpec = motion.fastEffectsSpec(),
                        )
                        when (row) {
                            is HomeRow.App -> ReorderableFavorite(
                                row = row,
                                icons = icons,
                                siblings = movable,
                                rows = rows.orEmpty(),
                                editing = editing,
                                onLaunch = { onLaunch(row.app) },
                                onLongPress = { onLongPress(row.app) },
                                onAppInfo = { onAppInfo(row.app) },
                                modifier = animated,
                            )
                            is HomeRow.NowPlaying -> NowPlayingCard(
                                media = row.media,
                                onTogglePlay = onTogglePlay,
                                onSkipNext = onSkipNext,
                                onSkipPrevious = onSkipPrevious,
                                onSeek = onSeek,
                                onPlayQueueItem = onPlayQueueItem,
                                onOpenOutput = onOpenOutput,
                                // No fade-out: a removed card leaves at once and its neighbours close the space.
                                modifier = Modifier.animateItem(
                                    fadeInSpec = motion.defaultEffectsSpec(),
                                    placementSpec = motion.defaultSpatialSpec(),
                                    fadeOutSpec = null,
                                ),
                            )
                            HomeRow.ListenerPrompt -> ListenerPromptRow(onGrantAccess, animated)
                            is NotificationRow -> NotificationPreviewRow(row, animated)
                            is CalendarRow -> CalendarCard(row, onOpenEvent, animated)
                            is WidgetHomeRow -> if (editing.active) {
                                ReorderableWidget(row, movable, rows.orEmpty(), editing, animated)
                            } else {
                                WidgetRow(
                                    row = row,
                                    createView = createWidgetView,
                                    onLongPress = { onWidgetLongPress(row) },
                                    modifier = animated,
                                )
                            }
                        }
                    }
                }
                // In edit mode the floating toolbar takes this place.
                if (!editing.active) SearchDock(onSearch, Modifier.padding(top = 8.dp, bottom = 8.dp))
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

/** Text shadow strength, per spec 5.4: the `scrim` role at 40 %. */
private const val SHADOW_ALPHA = 0.4f

/** The halo under home text when the scrim is on: the surface colour, soft and wide. */
private const val HALO_ALPHA = 0.55f

/** How far the scrim eases out into the empty space past the header and before the list. */
private val SCRIM_REACH = 120.dp
