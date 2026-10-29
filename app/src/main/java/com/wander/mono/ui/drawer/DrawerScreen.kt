package com.wander.mono.ui.drawer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.AppIconImage
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.data.settings.IconMode
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.SegmentedColumn
import com.wander.mono.ui.components.readableColumn
import com.wander.mono.ui.components.rememberOverscrollSwipe
import kotlinx.coroutines.launch

/**
 * All apps, alphabetical, with the letter rail; or, while a query is typed, the ranked results.
 * Swiping down past the top of the list closes it.
 */
@Composable
fun DrawerScreen(
    query: String,
    state: DrawerUiState,
    loadIcon: suspend (AppKey, IconMode, Int) -> AppIconImage?,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLaunchHit: (SearchHit) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    onClose: () -> Unit,
    onOpenSettings: () -> Unit,
    onUnlockHidden: () -> Unit,
    modifier: Modifier = Modifier,
    pickingApp: Boolean = false,
) {
    val icons = remember(state.iconMode, loadIcon) { AppIcons(state.iconMode, loadIcon) }
    val swipeDown = rememberOverscrollSwipe(upward = false, onSwipe = onClose)
    val bottom = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues()
    val topAndSides = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.windowInsetsPadding(topAndSides).readableColumn()) {
            SearchField(query, onQueryChange, onSubmit, onOpenSettings, Modifier.fillMaxWidth().padding(16.dp))
            if (pickingApp) {
                Text(
                    stringResource(R.string.edit_pick_hint),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                )
            }
            if (query.isBlank()) {
                AppList(state, icons, onLaunchApp, onLongPress, bottom, Modifier.nestedScroll(swipeDown))
            } else {
                SearchResultList(
                    results = state.results,
                    icons = icons,
                    onLaunchHit = onLaunchHit,
                    onLongPress = onLongPress,
                    contentPadding = bottom,
                    modifier = Modifier.nestedScroll(swipeDown),
                    onUnlockHidden = onUnlockHidden.takeIf { state.hiddenLocked },
                )
            }
        }
    }
}

@Composable
private fun AppList(
    state: DrawerUiState,
    icons: AppIcons,
    onLaunchApp: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    bottom: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    // One list item per letter, so the rail's sections and the list's items share their indexes.
    val groups = remember(state.apps, state.sections) { letterGroups(state.apps, state.sections) }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = RAIL_WIDTH,
                bottom = bottom.calculateBottomPadding() + 16.dp,
            ),
        ) {
            items(groups, key = { it.letter }) { group ->
                Column(Modifier.animateItem()) {
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
        }
        val current by remember { derivedStateOf { listState.firstVisibleItemIndex } }
        LetterRail(
            sections = state.sections,
            current = current,
            onSelect = { section ->
                scope.launch { listState.scrollToItem(state.sections.indexOf(section).coerceAtLeast(0)) }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(bottom = bottom.calculateBottomPadding() + 8.dp, top = 8.dp),
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
