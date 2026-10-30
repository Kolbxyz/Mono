package com.wander.mono.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.components.AppIcon
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.AppLabel

/**
 * An app in the drawer or in search results: icon and name, with what search matched in [name]
 * highlighted. Tap opens, long-press shows options. A new app ends in a small tertiary dot; an
 * archived one is greyed with a cloud, and opening it has the system restore it first.
 */
@Composable
fun DrawerAppRow(
    entry: DrawerEntry,
    icons: AppIcons,
    onLaunch: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 40.dp,
    labelStyle: TextStyle = MaterialTheme.typography.titleMedium,
    name: AnnotatedString = AnnotatedString(entry.app.label),
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
        val archived = entry.app.archived
        val dim = Modifier.alpha(if (archived) ARCHIVED_ALPHA else 1f)
        AppIcon(entry.app.key, icons, dim, size = iconSize)
        AppLabel(entry.app, entry.badge, style = labelStyle, name = name, modifier = dim.weight(1f, fill = false))
        if (archived) {
            Icon(
                painterResource(R.drawable.sym_archive),
                contentDescription = stringResource(R.string.state_archived),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
        if (entry.fresh) FreshDot()
    }
}

/** The dot after a new app's name: tertiary, so it never reads as the notification dot. */
@Composable
private fun FreshDot() {
    val spoken = stringResource(R.string.state_new_app)
    Box(
        Modifier
            .size(8.dp)
            .background(MaterialTheme.colorScheme.tertiary, CircleShape)
            .semantics { contentDescription = spoken },
    )
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
                    rememberHighlighted(hit.label, hit.matched),
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

/** How much of an archived app's icon and name shows: greyed, but still legible. */
private const val ARCHIVED_ALPHA = 0.5f

/** A full touch target even in text-only mode, where no icon props the row up (spec 9). */
internal val MIN_ROW_HEIGHT = 48.dp
