package com.wander.mono.ui.home

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchSource
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.calendar.CalendarEvent
import com.wander.mono.data.SwipeAction
import com.wander.mono.core.widgets.MonoWidgetHostView
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.edit.HomeEditing
import com.wander.mono.ui.edit.ReorderableFavorite
import com.wander.mono.ui.edit.ReorderableWidget
import com.wander.mono.ui.home.live.LiveActivityCard
import com.wander.mono.ui.edit.SwipeLeft
import com.wander.mono.ui.home.folders.FolderOverlays
import com.wander.mono.ui.home.folders.FolderRow
import com.wander.mono.ui.home.folders.FolderViewModel
import com.wander.mono.ui.home.folders.flattenFolders
import com.wander.mono.ui.home.folders.folderMemberKeys
import com.wander.mono.ui.home.folders.openFolders
import com.wander.mono.ui.theme.schemeAnimateItem

/**
 * Home's rows, anchored to the bottom of the list so favourites sit in the thumb zone: the empty
 * state until there is a favourite, then each row in shown order. The [lifted] favourites go first
 * while headphones are connected or a routine is on, outside edit mode only. The [pulse] favourite,
 * just added, pulses once. Open folders show their favourites under them, indented; edit mode
 * shows every favourite on its own, in the saved order, folders and all.
 */
@Composable
internal fun HomeList(
    rows: List<HomeRow>?,
    lifted: Set<AppKey>,
    listState: LazyListState,
    icons: AppIcons,
    editing: HomeEditing,
    previews: NotificationPreviewViewModel,
    pulse: AppKey?,
    onPulsed: (AppKey) -> Unit,
    contentPadding: PaddingValues,
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
    createWidgetView: (Context, Int) -> MonoWidgetHostView,
    onWidgetLongPress: (WidgetHomeRow) -> Unit,
    modifier: Modifier = Modifier,
    folders: FolderViewModel = viewModel(),
    extras: FavoriteExtrasViewModel = viewModel(),
) {
    val motion = MaterialTheme.motionScheme
    val open by folders.open.collectAsStateWithLifecycle()
    val swipes by extras.swipes.collectAsStateWithLifecycle()
    val overLimit by extras.overLimit.collectAsStateWithLifecycle()
    // Usage has no event: today's time is read again each time home comes back.
    LifecycleStartEffect(extras) {
        extras.recheckUsage()
        onStopOrDispose {}
    }
    FolderOverlays(folders)
    // Edit mode, and the moves it saves, work on the favourites themselves, never on folders.
    val flat = rows?.let(::flattenFolders)
    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom,
        contentPadding = contentPadding,
    ) {
        // The now-playing and prompt rows are not favourites: the empty state shows until there is one.
        if (flat != null && !editing.active && flat.none { it is HomeRow.App }) {
            item(key = "empty", contentType = "empty") {
                EmptyHome(onChooseFavorites = onOpenDrawer, modifier = schemeAnimateItem())
            }
        }
        val ordered = editing.shownRows(if (editing.showsSavedOrder) flat else rows)
        val movable = flattenFolders(ordered).filter { it is HomeRow.App || it is WidgetHomeRow }.map { it.key }
        // Saved order while editing; outside it, favourites chosen for headphones or a routine go first.
        val shown = if (editing.showsSavedOrder) ordered else openFolders(liftedFirst(ordered, lifted), open)
        val inFolder = if (editing.showsSavedOrder) emptySet<String>() else folderMemberKeys(ordered, open)
        items(shown, key = { it.key }, contentType = { it::class }) { row ->
            // The dragged row follows the finger, not a placement animation towards its new slot.
            val animated = if (editing.reorder.dragging == row.key) {
                Modifier.animateItem(
                    fadeInSpec = motion.defaultEffectsSpec(),
                    placementSpec = null,
                    fadeOutSpec = motion.fastEffectsSpec(),
                )
            } else {
                schemeAnimateItem()
            }
            when (row) {
                is HomeRow.App -> ReorderableFavorite(
                    row = row,
                    icons = icons,
                    siblings = movable,
                    rows = flat.orEmpty(),
                    editing = editing,
                    onLaunch = { source ->
                        if (row.key in inFolder) folders.closeAround(row.app.key)
                        onLaunch(row.app, source)
                    },
                    onLongPress = { onLongPress(row.app) },
                    onAppInfo = { onAppInfo(row.app) },
                    // Swiping right on a favourite with notifications expands its preview (spec 8).
                    onShowPreview = previewOf(rows, row)?.let { preview -> { previews.setExpanded(preview, true) } },
                    modifier = animated
                        .addedPulse(pulse == row.app.key) { onPulsed(row.app.key) }
                        .then(if (row.key in inFolder) Modifier.padding(start = FolderIndent) else Modifier),
                    swipeLeft = swipes[row.app.key]?.let { action ->
                        SwipeLeft(swipeLabel(action, row)) { extras.runSwipe(row.app.key, action) }
                    },
                    overLimit = row.app.key in overLimit,
                )
                is HomeRow.Folder -> FolderRow(
                    row = row,
                    icons = icons,
                    open = row.id in open,
                    onToggle = { folders.toggle(row.id) },
                    onOptions = { folders.showOptions(row.id) },
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
                is NotificationRow -> NotificationPreviewRow(row, animated, previews)
                is CalendarRow -> CalendarCard(row, onOpenEvent, animated)
                is LiveActivityRow -> LiveActivityCard(row, animated)
                is WidgetHomeRow -> if (editing.active) {
                    ReorderableWidget(row, movable, flat.orEmpty(), editing, animated)
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
}

/** What TalkBack calls [favorite]'s swipe left: App info, or opening the app's chosen shortcut. */
@Composable
private fun swipeLabel(action: SwipeAction, favorite: HomeRow.App): String = when (action) {
    SwipeAction.AppInfo -> stringResource(R.string.sheet_app_info)
    is SwipeAction.Shortcut -> stringResource(R.string.swipe_shortcut_action, favorite.app.label)
}

/** How far a folder's favourites sit in from the folder, so they read as inside it. */
private val FolderIndent = 28.dp

/** The preview row under [favorite], if it has one. */
private fun previewOf(rows: List<HomeRow>?, favorite: HomeRow.App): NotificationRow? =
    rows?.firstOrNull { it is NotificationRow && it.app.key == favorite.app.key } as NotificationRow?
