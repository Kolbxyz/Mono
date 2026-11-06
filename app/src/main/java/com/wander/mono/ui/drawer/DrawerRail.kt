package com.wander.mono.ui.drawer

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wander.mono.core.apps.PrivateSpaceState
import kotlinx.coroutines.launch

/**
 * The drawer list's letter rail, down the full height of the list on its edge, its entries spread
 * evenly from ★ at the top to the last letter just above the search field. Jumps put their entry at
 * the top of [listState]; with the private space ([space] and [privateActions]) a lock ends the rail.
 * The entry held under the finger goes to [hold], for the list to keep only that letter in view.
 */
@Composable
internal fun BoxScope.DrawerRail(
    state: DrawerUiState,
    listState: LazyListState,
    space: PrivateSpaceState.Present?,
    privateActions: PrivateSpaceActions?,
    hold: RailHold,
) {
    val scope = rememberCoroutineScope()
    val lead = state.railLead
    val atStart = state.railAtStart
    val sectionCount = state.sections.size
    val current by remember(listState, sectionCount, lead) {
        derivedStateOf {
            val first = listState.firstVisibleItemIndex
            RailEntries.railIndexFor(first, listState.firstVisibleItemScrollOffset, sectionCount, lead)
        }
    }
    // The private space ends the rail: a lock to scrub to, or tap to lock and unlock.
    val lock = if (space != null && privateActions != null) {
        RailLock(space.locked) { privateActions.onSetLocked(!space.locked) }
    } else {
        null
    }
    val privateItem = state.groups.size + state.work.size
    LetterRail(
        sections = state.sections,
        current = current,
        // ★ is the top of the list: the drawer has no favourites of its own to jump to.
        onSelect = { entry ->
            val item = if (lock != null && entry == sectionCount + 1) {
                privateItem
            } else {
                RailEntries.listItemFor(entry, lead)
            }
            scope.launch { listState.scrollToItem(item) }
        },
        modifier = Modifier
            .align(if (atStart) Alignment.CenterStart else Alignment.CenterEnd)
            .padding(vertical = RAIL_MARGIN),
        atStart = atStart,
        lock = lock,
        onHold = { hold.entry = it },
    )
}

/** The rail's gap from the list's top and bottom edges. */
private val RAIL_MARGIN = 8.dp
