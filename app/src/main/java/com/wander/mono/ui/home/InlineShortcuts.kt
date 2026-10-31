package com.wander.mono.ui.home

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import kotlinx.coroutines.flow.filter

/**
 * Home's inline shortcuts: which favourite has them unfolded, and what folds them again. Swiping
 * right on it again, a touch anywhere else on the list ([outsideTouch]), a scroll, edit mode and
 * leaving home all do; [insideTouch] marks the open favourite and its chips as the inside.
 */
@Stable
class InlineShortcuts internal constructor(
    internal val model: InlineShortcutViewModel,
    private val open: State<AppKey?>,
    private val byApp: State<Map<ShortcutOwner, List<AppShortcut>>>,
) {
    /** The touch that landed inside the open favourite, so the list's check below lets it be. */
    private var insideDown: PointerId? = null

    fun isOpen(app: AppKey): Boolean = open.value == app

    fun shortcutsOf(app: AppKey): List<AppShortcut> = byApp.value[ShortcutOwner.of(app)].orEmpty()

    /** A swipe right on [app]: false when it has no shortcuts, so nothing unfolds. */
    fun toggle(app: AppKey): Boolean = model.toggle(app)

    /**
     * On the list. Its final pass comes after every row's first one, so by then [insideTouch] has
     * claimed a touch on the open favourite; any other folds the shortcuts as it lands.
     */
    val outsideTouch: Modifier = Modifier.pointerInput(this) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Final)
            if (down.id != insideDown) model.close()
        }
    }

    /** On [app]'s favourite and its chips: a touch there, while they are open, is inside. */
    fun insideTouch(app: AppKey): Modifier = Modifier.pointerInput(this, app) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            if (open.value == app) insideDown = down.id
        }
    }
}

/** The [InlineShortcuts] of home's list in [listState], folded while [editing], on scroll and on leaving home. */
@Composable
internal fun rememberInlineShortcuts(
    listState: LazyListState,
    editing: Boolean,
    model: InlineShortcutViewModel = viewModel(),
): InlineShortcuts {
    val open = model.open.collectAsStateWithLifecycle()
    val byApp = model.byApp.collectAsStateWithLifecycle()
    LaunchedEffect(model, listState) {
        snapshotFlow { listState.isScrollInProgress }.filter { it }.collect { model.close() }
    }
    LaunchedEffect(model, editing) {
        if (editing) model.close()
    }
    LifecycleStartEffect(model) {
        onStopOrDispose { model.close() }
    }
    return remember(model, open, byApp) { InlineShortcuts(model, open, byApp) }
}
