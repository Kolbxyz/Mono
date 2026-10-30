package com.wander.mono.ui.edit

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wander.mono.core.apps.AppKey

/**
 * Edit mode for home's [list]: on while [active], its edits going to [edit]. [onHoldCancelled]
 * closes a favourite's options when its hold turns into a drag.
 */
@Composable
fun rememberHomeEditing(
    active: Boolean,
    list: LazyListState,
    edit: EditHomeViewModel,
    onEnter: () -> Unit,
    onHoldCancelled: () -> Unit,
    onRename: (AppKey) -> Unit,
): HomeEditing {
    val toggles by edit.toggles.collectAsStateWithLifecycle()
    val reorder = remember(list) { ReorderState(list) }
    return HomeEditing(
        active = active,
        toggles = toggles,
        reorder = reorder,
        onEnter = onEnter,
        onHoldCancelled = onHoldCancelled,
        onArrange = edit::arrange,
        onMove = edit::move,
        onRemove = edit::remove,
        onRename = onRename,
        onSetLiveRow = edit::setLiveRow,
        onResizeWidget = edit::resizeWidget,
        onRemoveWidget = edit::removeWidget,
    )
}
