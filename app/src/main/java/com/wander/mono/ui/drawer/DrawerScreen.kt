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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.AppIconImage
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.data.settings.IconMode
import com.wander.mono.ui.components.AppIcons
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
    modifier: Modifier = Modifier,
) {
    val icons = remember(state.iconMode, loadIcon) { AppIcons(state.iconMode, loadIcon) }
    val swipeDown = rememberOverscrollSwipe(upward = false, onSwipe = onClose)
    val bottom = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues()
    val topAndSides = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.windowInsetsPadding(topAndSides)) {
            SearchField(query, onQueryChange, onSubmit, onOpenSettings, Modifier.fillMaxWidth().padding(16.dp))
            if (query.isBlank()) {
                AppList(state, icons, onLaunchApp, onLongPress, bottom, Modifier.nestedScroll(swipeDown))
            } else {
                LazyColumn(Modifier.fillMaxSize().nestedScroll(swipeDown), contentPadding = bottom) {
                    if (state.results.isEmpty()) {
                        item(key = "none") {
                            Text(
                                stringResource(R.string.drawer_no_results),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                    items(state.results, key = { it.key }, contentType = { it::class }) { hit ->
                        when (hit) {
                            is SearchHit.App -> DrawerAppRow(
                                entry = hit.entry,
                                icons = icons,
                                onLaunch = { onLaunchHit(hit) },
                                onLongPress = { onLongPress(hit.entry.app) },
                            )
                            is SearchHit.Shortcut -> ShortcutRow(hit, icons, onLaunch = { onLaunchHit(hit) })
                        }
                    }
                }
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
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                end = RAIL_WIDTH,
                bottom = bottom.calculateBottomPadding(),
            ),
        ) {
            items(state.apps, key = { it.app.key.encode() }) { entry ->
                DrawerAppRow(
                    entry = entry,
                    icons = icons,
                    onLaunch = { onLaunchApp(entry.app) },
                    onLongPress = { onLongPress(entry.app) },
                )
            }
        }
        LetterRail(
            sections = state.sections,
            onSelect = { section -> scope.launch { listState.scrollToItem(section.index) } },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(bottom = bottom.calculateBottomPadding(), top = 8.dp),
        )
    }
}

private val RAIL_WIDTH = 32.dp
