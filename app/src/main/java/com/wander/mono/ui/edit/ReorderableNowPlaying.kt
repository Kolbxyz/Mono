package com.wander.mono.ui.edit

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.components.rememberMonoHaptics
import com.wander.mono.ui.home.HomeRow

/**
 * The now-playing card in edit mode, there even when nothing plays: dragged among the favourites
 * and widgets like a widget is (hold anywhere on it, or touch the handle). Dropped somewhere, it
 * stays there; once placed, its pin puts it back under the app that plays. [siblings] are the
 * movable rows' keys in shown order.
 */
@Composable
fun ReorderableNowPlaying(
    siblings: List<String>,
    rows: List<HomeRow>,
    editing: HomeEditing,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberMonoHaptics()
    val interactions = remember { MutableInteractionSource() }
    val reorder = editing.reorder
    val key = HomeRow.NowPlayingSpot.key
    val lifted = reorder.dragging == key
    val settle = rememberDropSettle()
    val settleSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val callbacks = rememberUpdatedState(
        FavoriteGestureCallbacks(
            editing = true,
            onTap = {},
            onSwipeRight = null,
            onSwipeLeft = null,
            onHeld = { haptics.longPress() },
            onHold = {},
            onLift = { reorder.start(key, siblings) },
            onDrag = { dy -> if (reorder.dragBy(dy)) haptics.segment() },
            onDrop = {
                if (reorder.dragging == key) {
                    haptics.gestureEnd()
                    settle.release(reorder.displacement, settleSpec)
                    editing.drop(rows)
                }
            },
        ),
    )
    Box(
        Modifier
            .reorderLift(lifted, reorder, settle)
            .then(modifier)
            .moveSemantics(key, siblings, rows, editing)
            .pointerInput(key) { detectFavoriteGestures(interactions, callbacks) },
    ) {
        EditNowPlayingRow(
            lifted = lifted,
            placed = editing.nowPlaying != null,
            onUnplace = editing.onUnplaceNowPlaying,
            handle = Modifier.pointerInput(key) { detectHandleDrag(callbacks) },
        )
    }
}

/** The card itself: drag handle, what it is, whether it follows its app, and the pin once placed. */
@Composable
private fun EditNowPlayingRow(lifted: Boolean, placed: Boolean, onUnplace: () -> Unit, handle: Modifier) {
    val lift = animateLift(lifted)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .liftScale(lift)
            .editCard(lift)
            .heightIn(min = 56.dp)
            .padding(end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DragHandle(handle)
        Icon(
            painterResource(R.drawable.sym_music_note),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 12.dp).size(24.dp),
        )
        Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
            Text(
                stringResource(R.string.live_row_now_playing),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                stringResource(if (placed) R.string.edit_now_playing_placed else R.string.edit_now_playing_follows),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
        if (placed) {
            IconButton(onClick = onUnplace) {
                Icon(
                    painterResource(R.drawable.sym_keep),
                    contentDescription = stringResource(R.string.edit_now_playing_unplace),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
