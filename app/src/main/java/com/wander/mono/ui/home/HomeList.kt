package com.wander.mono.ui.home

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchSource
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.calendar.CalendarEvent
import com.wander.mono.core.widgets.MonoWidgetHostView
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.edit.HomeEditing
import com.wander.mono.ui.edit.ReorderableFavorite
import com.wander.mono.ui.edit.ReorderableWidget
import com.wander.mono.ui.theme.schemeAnimateItem

/**
 * Home's rows, anchored to the bottom of the list so favourites sit in the thumb zone: the empty
 * state until there is a favourite, then each row in shown order. The [lifted] favourites go first
 * while headphones are connected, outside edit mode only. The [pulse] favourite, just added, pulses once.
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
) {
    val motion = MaterialTheme.motionScheme
    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom,
        contentPadding = contentPadding,
    ) {
        // The now-playing and prompt rows are not favourites: the empty state shows until there is one.
        if (rows != null && !editing.active && rows.none { it is HomeRow.App }) {
            item(key = "empty", contentType = "empty") {
                EmptyHome(onChooseFavorites = onOpenDrawer, modifier = schemeAnimateItem())
            }
        }
        val ordered = editing.shownRows(rows)
        val movable = ordered.filter { it is HomeRow.App || it is WidgetHomeRow }.map { it.key }
        // Saved order while editing; outside it, favourites chosen for headphones go first.
        val shown = if (editing.showsSavedOrder) ordered else liftedFirst(ordered, lifted)
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
                    rows = rows.orEmpty(),
                    editing = editing,
                    onLaunch = { source -> onLaunch(row.app, source) },
                    onLongPress = { onLongPress(row.app) },
                    onAppInfo = { onAppInfo(row.app) },
                    // Swiping right on a favourite with notifications expands its preview (spec 8).
                    onShowPreview = previewOf(rows, row)?.let { preview -> { previews.setExpanded(preview, true) } },
                    modifier = animated.addedPulse(pulse == row.app.key) { onPulsed(row.app.key) },
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
}

/** The preview row under [favorite], if it has one. */
private fun previewOf(rows: List<HomeRow>?, favorite: HomeRow.App): NotificationRow? =
    rows?.firstOrNull { it is NotificationRow && it.app.key == favorite.app.key } as NotificationRow?
