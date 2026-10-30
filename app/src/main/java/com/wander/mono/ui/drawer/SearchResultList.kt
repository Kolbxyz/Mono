package com.wander.mono.ui.drawer

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.data.settings.IconMode
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.SegmentGap
import com.wander.mono.ui.components.SegmentOuter
import com.wander.mono.ui.components.groupedListItem
import com.wander.mono.ui.theme.schemeAnimateItem

/**
 * The ranked results for a query, laid out by [SearchLayout]: the top hit large on a tonal container
 * (what the keyboard's Go key opens), the other apps as a row of icons, then shortcuts, contacts,
 * songs, settings and the web each as a labelled segmented group. Only [settled] results (built for
 * the query typed now) may say there are none.
 */
@Composable
fun SearchResultList(
    results: List<SearchHit>,
    settled: Boolean,
    icons: AppIcons,
    onLaunchHit: (SearchHit) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onUnlockHidden: (() -> Unit)? = null,
) {
    val layout = remember(results) { SearchLayout.of(results) }
    val listState = rememberLazyListState()
    // Keyed items would keep an old hit in view as new ones come in above it; at the top, stay there.
    LaunchedEffect(results) {
        if (listState.firstVisibleItemIndex == 0) listState.requestScrollToItem(0)
    }
    LazyColumn(
        modifier.fillMaxSize(),
        state = listState,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(SegmentGap),
    ) {
        val top = layout.top
        if (top == null) {
            if (settled) {
                item(key = "none") {
                    Text(
                        stringResource(R.string.drawer_no_results),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = schemeAnimateItem().padding(16.dp),
                    )
                }
            }
        } else {
            // One fixed key: a new top hit changes the card in place instead of two cards cross-fading.
            item(key = "top", contentType = "top") {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(SegmentOuter),
                    modifier = schemeAnimateItem().fillMaxWidth().padding(horizontal = 16.dp),
                ) {
                    val onLaunch = { onLaunchHit(top) }
                    TopHitRow(top, icons, onLaunch, onLongPress) { ResultRow(top, icons, onLaunch, onLongPress) }
                }
            }
        }
        if (layout.apps.isNotEmpty()) {
            item(key = "label:apps", contentType = "label") {
                GroupLabel(R.string.search_section_apps, schemeAnimateItem())
            }
            if (icons.mode == IconMode.TextOnly) {
                // No icons to make a row of: the apps are a group like the others.
                itemsIndexed(layout.apps, key = { _, hit -> hit.key }) { index, hit ->
                    GroupedRow(index, layout.apps.size) { ResultRow(hit, icons, { onLaunchHit(hit) }, onLongPress) }
                }
            } else {
                item(key = "apps", contentType = "apps") {
                    AppIconRow(layout.apps, icons, onLaunchHit, onLongPress, schemeAnimateItem())
                }
            }
        }
        layout.groups.forEach { group ->
            item(key = "label:${group.kind}", contentType = "label") {
                GroupLabel(group.kind.label, schemeAnimateItem())
            }
            itemsIndexed(group.hits, key = { _, hit -> hit.key }, contentType = { _, hit -> hit::class }) { i, hit ->
                GroupedRow(i, group.hits.size) { ResultRow(hit, icons, { onLaunchHit(hit) }, onLongPress) }
            }
        }
        if (onUnlockHidden != null) {
            item(key = "unlock-hidden") { UnlockHiddenRow(onUnlockHidden, schemeAnimateItem()) }
        }
    }
}

@Composable
private fun LazyItemScope.GroupedRow(
    index: Int,
    count: Int,
    content: @Composable () -> Unit,
) {
    Box(
        schemeAnimateItem().padding(horizontal = 16.dp).groupedListItem(index, count),
    ) {
        content()
    }
}

@Composable
private fun GroupLabel(@StringRes label: Int, modifier: Modifier = Modifier) {
    Text(
        stringResource(label),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .padding(start = 32.dp, end = 32.dp, top = 16.dp, bottom = 6.dp)
            .semantics { heading() },
    )
}

@Composable
private fun ResultRow(hit: SearchHit, icons: AppIcons, onLaunch: () -> Unit, onLongPress: (LaunchableApp) -> Unit) {
    when (hit) {
        is SearchHit.App -> DrawerAppRow(hit.entry, icons, onLaunch, onLongPress = { onLongPress(hit.entry.app) })
        is SearchHit.Shortcut -> ShortcutRow(hit, icons, onLaunch)
        is SearchHit.Calculation -> CalculationRow(hit, icons, onCopy = onLaunch)
        is SearchHit.Timer -> TimerRow(hit, icons, onSet = onLaunch)
        is SearchHit.Alarm -> AlarmRow(hit, icons, onSet = onLaunch)
        is SearchHit.Person -> PersonRow(hit, icons, onOpen = onLaunch)
        is SearchHit.Track -> TrackRow(hit, icons, onPlay = onLaunch)
        is SearchHit.Setting -> SettingRow(hit, icons, onOpen = onLaunch)
        is SearchHit.Web -> WebRow(hit, icons, onSearch = onLaunch)
    }
}
