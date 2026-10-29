package com.wander.mono.ui.drawer

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.components.AppIcon
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.AppLabel

/** An app in the drawer or in search results: icon and name. Tap opens, long-press shows options. */
@Composable
fun DrawerAppRow(
    entry: DrawerEntry,
    icons: AppIcons,
    onLaunch: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 40.dp,
    labelStyle: TextStyle = MaterialTheme.typography.titleMedium,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = MIN_ROW_HEIGHT)
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
        AppIcon(entry.app.key, icons, size = iconSize)
        AppLabel(entry.app, entry.badge, style = labelStyle)
    }
}

/**
 * A shortcut search result: its own icon badged with its app's, its label, and the app it belongs to
 * underneath. [ring] is the colour the row sits on, cut around the badge.
 */
@Composable
fun ShortcutRow(
    hit: SearchHit.Shortcut,
    icons: AppIcons,
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 40.dp,
    labelStyle: TextStyle = MaterialTheme.typography.titleMedium,
    ring: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = MIN_ROW_HEIGHT)
            .clickable(onClickLabel = stringResource(R.string.action_open), onClick = onLaunch)
            .unreadState(hit.badge)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShortcutIcon(hit, icons, size = iconSize, ring = ring)
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    hit.label,
                    style = labelStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                ShortcutBadge(hit.badge)
            }
            Text(
                hit.owner?.label ?: hit.shortcut.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** A full touch target even in text-only mode, where no icon props the row up (spec 9). */
internal val MIN_ROW_HEIGHT = 48.dp
