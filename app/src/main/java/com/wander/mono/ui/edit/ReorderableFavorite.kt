package com.wander.mono.ui.edit

import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchSource
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.PressMorph
import com.wander.mono.ui.components.expressivePress
import com.wander.mono.ui.components.rememberMonoHaptics
import com.wander.mono.ui.components.rememberRowAccent
import com.wander.mono.ui.home.FavoriteRow
import com.wander.mono.ui.home.HomeCardCorner
import com.wander.mono.ui.home.HomeRow
import com.wander.mono.ui.home.InlineShortcutStrip
import com.wander.mono.ui.home.InlineShortcuts
import com.wander.mono.ui.home.LocalRowDensity
import com.wander.mono.ui.home.favoriteRowMetrics
import com.wander.mono.ui.home.rememberInlineShortcutNope
import com.wander.mono.ui.home.rememberLaunchOrigin

/**
 * A favourite on home, in or out of edit mode. The gestures live here, around both looks, so a
 * hold-and-drag that starts edit mode carries on as the row turns into its edit form.
 * [siblings] are the movable rows' keys (favourites and, in edit mode, widgets) in shown order:
 * what a drag moves among. A swipe right unfolds its [inline] shortcuts under it or, when it has
 * none, expands its notification preview ([onShowPreview], null when it has none); with neither,
 * the row bounces back. [swipeLeft] is what the user chose for a swipe left, null for nothing.
 * [overLimit] dims it: today's time in the app is past its daily limit.
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
    swipeLeft: SwipeLeft? = null,
    overLimit: Boolean = false,
    inline: InlineShortcuts? = null,
) {
    val haptics = rememberMonoHaptics()
    val nope = rememberInlineShortcutNope()
    val interactions = remember { MutableInteractionSource() }
    // With "Tint rows with app colour", the press ripples in the app's colour; else the theme's.
    val accent = rememberRowAccent(row.app.key, icons, favoriteRowMetrics(LocalRowDensity.current).iconSize)
    val reorder = editing.reorder
    val lifted = reorder.dragging == row.key
    val settle = rememberDropSettle()
    val settleSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    // The app's window grows out of this row (spec 7.4.4).
    val origin = rememberLaunchOrigin()
    val launch = { onLaunch(origin.source()) }
    val actions = FavoriteActions(launch, onLongPress, onAppInfo, onShowPreview, swipeLeft, inline)
    val callbacks = rememberUpdatedState(
        FavoriteGestureCallbacks(
            editing = editing.active,
            onTap = launch,
            onSwipeRight = {
                when {
                    inline?.toggle(row.app.key) == true -> haptics.threshold()
                    onShowPreview != null -> {
                        haptics.threshold()
                        onShowPreview()
                    }
                    else -> nope.play()
                }
            },
            onSwipeLeft = swipeLeft?.let { swipe ->
                {
                    haptics.threshold()
                    swipe.run()
                }
            },
            onHeld = { haptics.longPress() },
            onHold = onLongPress,
            onHoldCancelled = editing.onHoldCancelled,
            onLift = {
                if (!editing.active) editing.onEnter()
                reorder.start(row.key, siblings)
            },
            onDrag = { dy -> if (reorder.dragBy(dy)) haptics.segment() },
            onDrop = {
                if (reorder.dragging == row.key) {
                    haptics.gestureEnd()
                    settle.release(reorder.displacement, settleSpec)
                    editing.drop(rows)
                }
            },
        ),
    )
    // The edit form draws its own container and lift shadow, which the press's clip would cut off.
    val press = if (editing.active) {
        Modifier
    } else {
        Modifier.expressivePress(interactions, PressMorph.squarer(HomeCardCorner))
    }
    // Lift and list placement on the root, so the chips under the row move with it.
    Column(
        Modifier
            .reorderLift(lifted, reorder, settle)
            .then(modifier)
            .then(inline?.insideTouch(row.app.key) ?: Modifier),
    ) {
        Box(
            Modifier
                .then(nope.modifier)
                .then(origin.modifier)
                .favoriteSemantics(row, siblings, rows, editing, actions)
                .then(press)
                .indication(interactions, if (editing.active) null else ripple(color = accent ?: Color.Unspecified))
                .pointerInput(row.key) { detectFavoriteGestures(interactions, callbacks) },
        ) {
            if (editing.active) {
                EditFavoriteRow(
                    row = row,
                    icons = icons,
                    toggles = editing.toggles[row.app.key].orEmpty(),
                    lifted = lifted,
                    onRename = { editing.onRename(row.app.key) },
                    onRemove = { editing.onRemove(row.app.key) },
                    onSetLiveRow = editing.onSetLiveRow,
                    handle = Modifier.pointerInput(row.key) { detectHandleDrag(callbacks) },
                )
            } else {
                FavoriteRow(row, icons, overLimit = overLimit)
            }
        }
        if (inline != null) InlineShortcutStrip(inline, row.app, icons)
    }
}

/**
 * Outside edit mode the row is one button (open, options) offering App info, its shortcuts and
 * Show notifications (the swipe right), the swipe left's action and the moves among the favourites as custom
 * actions, so TalkBack can reorder without entering edit mode. In edit mode its buttons and
 * switches stay separate nodes, and the row offers TalkBack the moves a drag does, among
 * favourites and widgets alike.
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
    val shortcutsOpen = actions.inline?.isOpen(row.app.key) == true
    val shortcutsLabel =
        stringResource(if (shortcutsOpen) R.string.favorite_hide_shortcuts else R.string.favorite_show_shortcuts)
    val favorites = rows.filterIsInstance<HomeRow.App>()
    val index = favorites.indexOfFirst { it.key == row.key }
    val key = row.app.key
    return semantics(mergeDescendants = true) {
        onClick(openLabel) { actions.onLaunch().let { true } }
        onLongClick(optionsLabel) { actions.onLongPress().let { true } }
        customActions = buildList {
            add(CustomAccessibilityAction(appInfo) { actions.onAppInfo().let { true } })
            actions.inline?.takeIf { it.shortcutsOf(key).isNotEmpty() }?.let { inline ->
                add(CustomAccessibilityAction(shortcutsLabel) { inline.toggle(key) })
            }
            actions.onShowPreview?.let { show -> add(CustomAccessibilityAction(showPreview) { show().let { true } }) }
            actions.swipeLeft?.let { swipe -> add(CustomAccessibilityAction(swipe.label) { swipe.run().let { true } }) }
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
    val swipeLeft: SwipeLeft?,
    val inline: InlineShortcuts?,
)
