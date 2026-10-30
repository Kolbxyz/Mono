package com.wander.mono.ui.drawer

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.contacts.ContactReach
import com.wander.mono.ui.components.AppIcon
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.PressMorph
import com.wander.mono.ui.components.expressivePress
import com.wander.mono.ui.components.instantPress
import com.wander.mono.ui.theme.schemeAnimateItem

/**
 * The top hit, larger than the rows under it: a bigger icon and title for an app or a shortcut, a
 * contact's actions in a colour that stands out from the card; any other result as its usual row.
 */
@Composable
internal fun TopHitRow(
    hit: SearchHit,
    icons: AppIcons,
    onLaunch: () -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    onReach: ((SearchHit.Person, ContactReach) -> Unit)?,
    other: @Composable () -> Unit,
) {
    val title = MaterialTheme.typography.titleLarge
    when (hit) {
        is SearchHit.App -> DrawerAppRow(
            hit.entry,
            icons,
            onLaunch = onLaunch,
            onLongPress = { onLongPress(hit.entry.app) },
            modifier = Modifier.padding(vertical = 4.dp),
            iconSize = TOP_ICON,
            labelStyle = title,
            name = rememberHighlighted(hit.label, hit.matched),
        )
        is SearchHit.Shortcut -> ShortcutRow(
            hit,
            icons,
            onLaunch = onLaunch,
            modifier = Modifier.padding(vertical = 4.dp),
            iconSize = TOP_ICON,
            labelStyle = title,
            ring = MaterialTheme.colorScheme.secondaryContainer,
        )
        is SearchHit.Person -> PersonRow(
            hit,
            icons,
            onOpen = onLaunch,
            onReach = onReach?.let { reach -> { how: ContactReach -> reach(hit, how) } },
            chipColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
        else -> other()
    }
}

/**
 * The apps after the top hit, side by side as icons with their names under them, scrolling sideways;
 * back to the first whenever the apps change.
 */
@Composable
internal fun AppIconRow(
    apps: List<SearchHit.App>,
    icons: AppIcons,
    onLaunchHit: (SearchHit) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rowState = rememberLazyListState()
    LaunchedEffect(apps) { rowState.requestScrollToItem(0) }
    LazyRow(
        modifier,
        state = rowState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(apps, key = { it.key }) { hit ->
            AppTile(
                hit.entry.app,
                hit.matched,
                icons,
                onLaunch = { onLaunchHit(hit) },
                onLongPress = { onLongPress(hit.entry.app) },
                modifier = schemeAnimateItem(),
            )
        }
    }
}

@Composable
private fun AppTile(
    app: LaunchableApp,
    matched: List<IntRange>,
    icons: AppIcons,
    onLaunch: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier
            .width(TILE_WIDTH)
            // A tile is a small card: its corners tighten as it shrinks under the finger.
            .expressivePress(interaction, PressMorph.squarer(TILE_CORNER))
            .instantPress(interaction)
            .combinedClickable(
                interactionSource = interaction,
                indication = ripple(),
                onClickLabel = stringResource(R.string.action_open),
                onLongClickLabel = stringResource(R.string.action_app_options),
                onLongClick = onLongPress,
                onClick = onLaunch,
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AppIcon(app.key, icons, size = TILE_ICON)
        Text(
            rememberHighlighted(app.label, matched),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

private val TOP_ICON = 56.dp
private val TILE_ICON = 52.dp
private val TILE_WIDTH = 80.dp
private val TILE_CORNER = 16.dp
