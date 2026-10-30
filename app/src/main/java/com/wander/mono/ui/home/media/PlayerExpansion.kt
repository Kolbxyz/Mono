package com.wander.mono.ui.home.media

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import kotlinx.coroutines.flow.filter

/**
 * Whether the player under the clock shows in full or as its strip. It collapses by itself on a
 * tap outside it, on back and when home's list scrolls ([PlayerCollapseEffects]).
 */
@Stable
class PlayerExpansion internal constructor(initial: Boolean) {
    var expanded by mutableStateOf(initial)
        private set

    /** Where the player is, in root coordinates, so a tap on it is not a tap outside. */
    private var bounds = Rect.Zero

    /** Where the host that watches for taps outside is, in root coordinates. */
    private var hostOrigin = Offset.Zero

    fun toggle() {
        expanded = !expanded
    }

    fun collapse() {
        expanded = false
    }

    internal fun reset(to: Boolean) {
        expanded = to
    }

    internal fun onPlayerPlaced(coordinates: LayoutCoordinates) {
        bounds = coordinates.boundsInRoot()
    }

    internal fun onHostPlaced(coordinates: LayoutCoordinates) {
        hostOrigin = coordinates.positionInRoot()
    }

    /** Collapses for a press at [position] in the host when it lands outside the player. */
    internal fun pressedInHost(position: Offset) {
        if (expanded && !bounds.contains(position + hostOrigin)) collapse()
    }

    companion object {
        internal val Saver = Saver<PlayerExpansion, Boolean>(save = { it.expanded }, restore = ::PlayerExpansion)
    }
}

/**
 * The player's expansion, starting [expandedByDefault] (the user's choice; changing it applies at
 * once) and kept across configuration changes.
 */
@Composable
fun rememberPlayerExpansion(expandedByDefault: Boolean): PlayerExpansion =
    rememberSaveable(expandedByDefault, saver = PlayerExpansion.Saver) { PlayerExpansion(expandedByDefault) }

/**
 * What collapses the expanded player: back (while home is in view, not [covered]), a scroll of
 * home's [list], and covering home (the drawer or settings), after which it opens as it starts,
 * [expandedByDefault].
 */
@Composable
fun PlayerCollapseEffects(
    expansion: PlayerExpansion,
    list: LazyListState,
    covered: Boolean,
    expandedByDefault: Boolean,
) {
    BackHandler(enabled = expansion.expanded && !covered) { expansion.collapse() }
    LaunchedEffect(expansion, list) {
        snapshotFlow { list.isScrollInProgress }.filter { it }.collect { expansion.collapse() }
    }
    LaunchedEffect(expansion, covered) {
        if (covered) expansion.reset(expandedByDefault)
    }
}

/**
 * Watches every press on what it modifies, before its children see it and without consuming it,
 * so a press outside the player collapses it and still does what it would have done.
 */
fun Modifier.collapsesPlayerOutside(expansion: PlayerExpansion): Modifier = this
    .onGloballyPositioned(expansion::onHostPlaced)
    .pointerInput(expansion) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            expansion.pressedInHost(down.position)
        }
    }

/** Reports where the player is, so presses on it are not taken as outside it. */
fun Modifier.playerBounds(expansion: PlayerExpansion): Modifier = onGloballyPositioned(expansion::onPlayerPlaced)
