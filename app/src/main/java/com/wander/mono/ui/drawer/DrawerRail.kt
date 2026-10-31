package com.wander.mono.ui.drawer

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.wander.mono.core.apps.PrivateSpaceState
import kotlinx.coroutines.launch

/**
 * The drawer list's letter rail, set low on its edge in the thumb's reach: only as tall as its
 * entries need (one comfortable slot each), resting on the bottom of the list's [available] height
 * just above the search dock, so ★ and the first letters are no stretch for the hand holding the
 * phone. Jumps land on [listState]; with the private space ([space] and [privateActions]) a lock
 * ends the rail.
 */
@Composable
internal fun BoxScope.DrawerRail(
    state: DrawerUiState,
    listState: LazyListState,
    available: Dp,
    space: PrivateSpaceState.Present?,
    privateActions: PrivateSpaceActions?,
    railScrub: RailScrub?,
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
    // ★, the letters and the lock, each a slot at least one label line tall at the user's font scale.
    val entries = 1 + sectionCount + (if (lock != null) 1 else 0)
    val line = with(LocalDensity.current) { MaterialTheme.typography.labelMedium.lineHeight.toDp() }
    val wanted = max(RAIL_SLOT, line) * entries + RAIL_ENDS
    val railHeight = wanted.coerceAtMost(available - RAIL_MARGIN * 2).coerceAtLeast(0.dp)
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
            .align(if (atStart) Alignment.BottomStart else Alignment.BottomEnd)
            .padding(vertical = RAIL_MARGIN)
            .height(railHeight),
        atStart = atStart,
        lock = lock,
        scrub = railScrub,
    )
}

/** Each rail entry's slot: a thumb can tell neighbours apart, and the whole rail stays low. */
private val RAIL_SLOT = 19.dp

/** The track's padding above the first entry and below the last, inside the rail. */
private val RAIL_ENDS = 16.dp

/** The rail's gap from the list's top and bottom edges. */
private val RAIL_MARGIN = 8.dp
