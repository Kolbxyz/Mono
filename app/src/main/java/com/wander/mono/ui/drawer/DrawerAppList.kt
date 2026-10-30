package com.wander.mono.ui.drawer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.apps.PrivateSpaceState
import com.wander.mono.core.apps.WorkProfile
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.SegmentedColumn
import com.wander.mono.ui.theme.schemeAnimateItem
import kotlinx.coroutines.launch

/**
 * The main profile's apps under their letters, with the letter rail, then a card per work profile
 * and the private space at the very bottom; no private space without [privateActions] (while
 * picking an app for home, where private apps never go). [listState] is the caller's, so the list
 * keeps its place while search results show instead. Dragging up once the list has nowhere further
 * to go runs [onPullUp]. TalkBack gets a "Jump to" action per letter on the list itself.
 */
@Composable
internal fun DrawerAppList(
    state: DrawerUiState,
    icons: AppIcons,
    privateActions: PrivateSpaceActions?,
    onSetWorkPaused: (WorkProfile, Boolean) -> Unit,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    bottom: PaddingValues,
    listState: LazyListState,
    onPullUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    // One list item per letter, so the rail's sections and the list's items share their indexes.
    val groups = remember(state.apps, state.sections) { letterGroups(state.apps, state.sections) }
    val resources = LocalResources.current
    val jumpActions = remember(state.sections, resources, listState) {
        state.sections.mapIndexed { item, section ->
            CustomAccessibilityAction(resources.getString(R.string.drawer_jump_to_letter, section.letter)) {
                scope.launch { listState.scrollToItem(item) }
                true
            }
        }
    }
    val searchPull = rememberSearchPullConnection(onPullUp)
    val atStart = state.railAtStart
    Box(modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(searchPull)
                .semantics { customActions = jumpActions },
            contentPadding = PaddingValues(
                start = if (atStart) RAIL_WIDTH else 16.dp,
                end = if (atStart) 16.dp else RAIL_WIDTH,
                bottom = bottom.calculateBottomPadding() + 16.dp,
            ),
        ) {
            items(groups, key = { it.letter }) { group ->
                Column(schemeAnimateItem()) {
                    Text(
                        group.letter,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                            .semantics { heading() },
                    )
                    SegmentedColumn {
                        group.entries.forEach { entry ->
                            DrawerAppRow(
                                entry = entry,
                                icons = icons,
                                onLaunch = { onLaunchApp(entry.app) },
                                onLongPress = { onLongPress(entry.app) },
                            )
                        }
                    }
                }
            }
            // After the letters, so the rail's indexes still match the list's.
            items(state.work, key = { "work:${it.profile.serial}" }) { section ->
                WorkSectionCard(section, icons, onSetWorkPaused, onLaunchApp, onLongPress, schemeAnimateItem())
            }
            val space = state.privateSpace
            if (privateActions != null && space is PrivateSpaceState.Present) {
                item(key = "private") {
                    PrivateSection(
                        space,
                        state.privateApps,
                        icons,
                        privateActions,
                        onLaunchApp,
                        onLongPress,
                        schemeAnimateItem(),
                    )
                }
            }
        }
        val sectionCount = state.sections.size
        val current by remember(listState, sectionCount) {
            derivedStateOf {
                val first = listState.firstVisibleItemIndex
                RailMath.railIndexFor(first, listState.firstVisibleItemScrollOffset, sectionCount)
            }
        }
        // Not the keyboard: the rail would re-measure, and its letters reflow, every frame it slides.
        val navigationBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        LetterRail(
            sections = state.sections,
            current = current,
            // ★ is the top of the list: the drawer has no favourites of its own to jump to.
            onSelect = { entry -> scope.launch { listState.scrollToItem(RailMath.listItemFor(entry)) } },
            modifier = Modifier
                .align(if (atStart) Alignment.CenterStart else Alignment.CenterEnd)
                .padding(bottom = navigationBottom + 8.dp, top = 8.dp),
            atStart = atStart,
        )
    }
}

/** The apps under one letter of the rail. */
private data class LetterGroup(val letter: String, val entries: List<DrawerEntry>)

/** Splits [apps] at each [Section]'s first index; every app falls under exactly one letter. */
private fun letterGroups(apps: List<DrawerEntry>, sections: List<Section>): List<LetterGroup> =
    sections.mapIndexed { i, section ->
        val end = sections.getOrNull(i + 1)?.index ?: apps.size
        LetterGroup(section.letter, apps.subList(section.index, end))
    }
