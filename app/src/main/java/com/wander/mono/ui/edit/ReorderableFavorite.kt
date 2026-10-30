package com.wander.mono.ui.edit

import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchSource
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.pressShrink
import com.wander.mono.ui.home.FavoriteRow
import com.wander.mono.ui.home.HomeRow
import com.wander.mono.ui.home.rememberLaunchOrigin

/**
 * A favourite on home, in or out of edit mode. The gestures live here, around both looks, so a
 * hold-and-drag that starts edit mode carries on as the row turns into its edit form.
 * [siblings] are the movable rows' keys (favourites and, in edit mode, widgets) in shown order:
 * what a drag moves among. [onShowPreview] expands the favourite's notification preview, null
 * when it has none.
 */
@Composable
fun ReorderableFavorite(
    row: HomeRow.App,
    icons: AppIcons,
    siblings: List<String>,
    rows: List<HomeRow>,
    editing: HomeEditing,
    onLaunch: (LaunchSource?) -> Unit,
    onLongPress: () -> Unit,
    onAppInfo: () -> Unit,
    onShowPreview: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val interactions = remember { MutableInteractionSource() }
    val reorder = editing.reorder
    val lifted = reorder.dragging == row.key
    val settle = rememberDropSettle()
    val settleSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    // The app's window grows out of this row (spec 7.4.4).
    val origin = rememberLaunchOrigin()
    val launch = { onLaunch(origin.source()) }
    val actions = FavoriteActions(launch, onLongPress, onAppInfo, onShowPreview)
    val callbacks = rememberUpdatedState(
        FavoriteGestureCallbacks(
            editing = editing.active,
            onTap = launch,
            onSwipeRight = onShowPreview?.let { show ->
                {
                    haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                    show()
                }
            },
            onHeld = { haptics.performHapticFeedback(HapticFeedbackType.LongPress) },
            onHold = onLongPress,
            onLift = {
                if (!editing.active) editing.onEnter()
                reorder.start(row.key, siblings)
            },
            onDrag = { dy -> if (reorder.dragBy(dy)) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick) },
            onDrop = {
                if (reorder.dragging == row.key) {
                    haptics.performHapticFeedback(HapticFeedbackType.GestureEnd)
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
            .then(origin.modifier)
            .favoriteSemantics(row, siblings, rows, editing, actions)
            // The edit form draws its own container and lift shadow, which a clip would cut off.
            .then(if (editing.active) Modifier else Modifier.pressShrink(interactions).clip(MaterialTheme.shapes.large))
            .indication(interactions, if (editing.active) null else ripple())
            .pointerInput(row.key) { detectFavoriteGestures(interactions, callbacks) },
    ) {
        if (editing.active) {
            EditFavoriteRow(
                row = row,
                toggles = editing.toggles[row.app.key].orEmpty(),
                lifted = lifted,
                onRename = { editing.onRename(row.app.key) },
                onRemove = { editing.onRemove(row.app.key) },
                onSetLiveRow = editing.onSetLiveRow,
                handle = Modifier.pointerInput(row.key) { detectHandleDrag(callbacks) },
            )
        } else {
            FavoriteRow(row, icons)
        }
    }
}

/**
 * Outside edit mode the row is one button (open, options) offering App info, Show notifications
 * (the swipe right) and the moves among the favourites as custom actions, so TalkBack can reorder
 * without entering edit mode. In edit mode its buttons and switches stay separate nodes, and the
 * row offers TalkBack the moves a drag does, among favourites and widgets alike.
 */
@Composable
private fun Modifier.favoriteSemantics(
    row: HomeRow.App,
    siblings: List<String>,
    rows: List<HomeRow>,
    editing: HomeEditing,
    actions: FavoriteActions,
): Modifier {
    if (editing.active) return moveSemantics(row.key, siblings, rows, editing)
    val openLabel = stringResource(R.string.action_open)
    val optionsLabel = stringResource(R.string.action_app_options)
    val moveUp = stringResource(R.string.sheet_move_up)
    val moveDown = stringResource(R.string.sheet_move_down)
    val appInfo = stringResource(R.string.sheet_app_info)
    val showPreview = stringResource(R.string.favorite_show_notifications)
    val favorites = rows.filterIsInstance<HomeRow.App>()
    val index = favorites.indexOfFirst { it.key == row.key }
    val key = row.app.key
    return semantics(mergeDescendants = true) {
        onClick(openLabel) { actions.onLaunch().let { true } }
        onLongClick(optionsLabel) { actions.onLongPress().let { true } }
        customActions = buildList {
            add(CustomAccessibilityAction(appInfo) { actions.onAppInfo().let { true } })
            actions.onShowPreview?.let { show -> add(CustomAccessibilityAction(showPreview) { show().let { true } }) }
            if (index > 0) add(CustomAccessibilityAction(moveUp) { editing.onMove(key, -1).let { true } })
            if (index in 0 until favorites.lastIndex) {
                add(CustomAccessibilityAction(moveDown) { editing.onMove(key, 1).let { true } })
            }
        }
    }
}

/** What TalkBack can do with a favourite outside edit mode. */
private class FavoriteActions(
    val onLaunch: () -> Unit,
    val onLongPress: () -> Unit,
    val onAppInfo: () -> Unit,
    val onShowPreview: (() -> Unit)?,
)
