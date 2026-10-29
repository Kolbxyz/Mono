package com.wander.mono.ui.drawer

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.AppKey
import com.wander.mono.ui.components.AppIcon
import com.wander.mono.ui.components.AppLabel

/** An app in the drawer or in search results: icon and name. Tap opens, long-press shows options. */
@Composable
fun DrawerAppRow(
    entry: DrawerEntry,
    loadIcon: suspend (AppKey, Int) -> ImageBitmap?,
    onLaunch: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .combinedClickable(
                onClickLabel = stringResource(R.string.action_open),
                onLongClickLabel = stringResource(R.string.action_app_options),
                onLongClick = onLongPress,
                onClick = onLaunch,
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(entry.app.key, loadIcon)
        AppLabel(entry.app, entry.badge, style = MaterialTheme.typography.titleMedium)
    }
}

/** A shortcut search result: its label, and the app it belongs to underneath. */
@Composable
fun ShortcutRow(
    hit: SearchHit.Shortcut,
    loadIcon: suspend (AppKey, Int) -> ImageBitmap?,
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clickable(onClickLabel = stringResource(R.string.action_open), onClick = onLaunch)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        hit.owner?.let { AppIcon(it.key, loadIcon) } ?: Spacer(Modifier.size(40.dp))
        Column {
            Text(
                hit.label,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                hit.owner?.label ?: hit.shortcut.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}
