package com.wander.mono.ui.drawer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.components.AppIcons

/**
 * The ranked results for a query. The first sits on a tonal container: it is what the keyboard's
 * Go key opens.
 */
@Composable
fun SearchResultList(
    results: List<SearchHit>,
    icons: AppIcons,
    onLaunchHit: (SearchHit) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onUnlockHidden: (() -> Unit)? = null,
) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = contentPadding) {
        if (results.isEmpty()) {
            item(key = "none") {
                Text(
                    stringResource(R.string.drawer_no_results),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        itemsIndexed(results, key = { _, hit -> hit.key }, contentType = { _, hit -> hit::class }) { index, hit ->
            val top = index == 0
            Surface(
                color = if (top) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            ) {
                ResultRow(hit, icons, onLaunch = { onLaunchHit(hit) }, onLongPress = onLongPress)
            }
        }
        if (onUnlockHidden != null) item(key = "unlock-hidden") { UnlockHiddenRow(onUnlockHidden) }
    }
}

@Composable
private fun ResultRow(hit: SearchHit, icons: AppIcons, onLaunch: () -> Unit, onLongPress: (LaunchableApp) -> Unit) {
    when (hit) {
        is SearchHit.App -> DrawerAppRow(hit.entry, icons, onLaunch, onLongPress = { onLongPress(hit.entry.app) })
        is SearchHit.Shortcut -> ShortcutRow(hit, icons, onLaunch)
        is SearchHit.Calculation -> CalculationRow(hit, icons, onCopy = onLaunch)
        is SearchHit.Person -> PersonRow(hit, icons, onOpen = onLaunch)
        is SearchHit.Setting -> SettingRow(hit, icons, onOpen = onLaunch)
        is SearchHit.Web -> WebRow(hit, icons, onSearch = onLaunch)
    }
}
