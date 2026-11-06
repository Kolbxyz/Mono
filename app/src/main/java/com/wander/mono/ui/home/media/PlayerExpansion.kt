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
 * Whether the player under the clock shows in full or as its strip. Resting as a strip
 * ([restsExpanded] false), once opened it folds back by itself on a tap outside it, on back and when
 * home's list scrolls ([PlayerCollapseEffects]). Resting in full (the user's choice), it stays as it
 * is until the user taps it, as Niagara's card does.
 */
@Stable
class PlayerExpansion internal constructor(initial: Boolean, internal val restsExpanded: Boolean = initial) {
    var expanded by mutableStateOf(initial)
        private set

    /** Expanded for now only, over a strip at rest: what an outside tap, back or a scroll folds. */
    val openedForNow: Boolean get() = PlayerFolding.foldsByItself(expanded, restsExpanded)

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

    /** Folds the player back to its strip, only when it was opened for now ([openedForNow]). */
    internal fun foldBack() {
        if (openedForNow) collapse()
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

    /** Folds back for a press at [position] in the host when it lands outside the player. */
    internal fun pressedInHost(position: Offset) {
        if (!bounds.contains(position + hostOrigin)) foldBack()
    }

    companion object {
        /** Saves whether it is expanded; [rememberPlayerExpansion] keys on the resting state itself. */
        internal fun saver(restsExpanded: Boolean) = Saver<PlayerExpansion, Boolean>(
            save = { it.expanded },
            restore = { PlayerExpansion(it, restsExpanded) },
        )
    }
}

/** When the player folds back by itself: only when it is open over a strip at rest. */
internal object PlayerFolding {
    fun foldsByItself(expanded: Boolean, restsExpanded: Boolean): Boolean = expanded && !restsExpanded
}

/**
 * The player's expansion, starting [expandedByDefault] (the user's choice; changing it applies at
 * once) and kept across configuration changes.
 */
@Composable
fun rememberPlayerExpansion(expandedByDefault: Boolean): PlayerExpansion =
    rememberSaveable(expandedByDefault, saver = PlayerExpansion.saver(expandedByDefault)) {
        PlayerExpansion(expandedByDefault)
    }

/**
 * What folds a player opened for now back to its strip: back (while home is in view, not
 * [covered]) and a scroll of home's [list]; a player resting in full is left alone. Covering home
 * (the drawer or settings) puts it back as it starts, [expandedByDefault], either way.
 */
@Composable
fun PlayerCollapseEffects(
    expansion: PlayerExpansion,
    list: LazyListState,
    covered: Boolean,
    expandedByDefault: Boolean,
) {
    // Back is only the player's while it has something to fold; otherwise home keeps it.
    BackHandler(enabled = expansion.openedForNow && !covered) { expansion.foldBack() }
    LaunchedEffect(expansion, list) {
        snapshotFlow { list.isScrollInProgress }.filter { it }.collect { expansion.foldBack() }
    }
    LaunchedEffect(expansion, covered) {
        if (covered) expansion.reset(expandedByDefault)
    }
}

/**
 * Watches every press on what it modifies, before its children see it and without consuming it,
 * so a press outside a player opened for now folds it back and still does what it would have done.
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
