package com.wander.mono.ui.home.folders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.components.AppBadge
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.CountBadge
import com.wander.mono.ui.components.NotificationDot
import com.wander.mono.ui.components.PressMorph
import com.wander.mono.ui.components.expressivePress
import com.wander.mono.ui.components.instantPress
import com.wander.mono.ui.home.FavoriteIconGap
import com.wander.mono.ui.home.FavoriteRowInset
import com.wander.mono.ui.home.HomeCardCorner
import com.wander.mono.ui.home.HomeRow
import com.wander.mono.ui.home.LocalRowDensity
import com.wander.mono.ui.home.badgeState
import com.wander.mono.ui.home.favoriteRowMetrics
import com.wander.mono.ui.home.onWallpaper
import com.wander.mono.ui.theme.secondaryTextColor

/**
 * A folder on home, sized like a favourite: a tonal tile of its first apps' icons, its name and
 * badge, the names inside while it is closed, and a chevron that turns as it opens. A tap opens
 * or closes it (its apps then show under it, indented); a hold offers rename and ungroup.
 */
@Composable
internal fun FolderRow(
    row: HomeRow.Folder,
    icons: AppIcons,
    open: Boolean,
    onToggle: () -> Unit,
    onOptions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = favoriteRowMetrics(LocalRowDensity.current)
    val motion = MaterialTheme.motionScheme
    val interactions = remember { MutableInteractionSource() }
    val turn by animateFloatAsState(if (open) HALF_TURN else 0f, motion.defaultSpatialSpec())
    val state = listOfNotNull(
        stringResource(if (open) R.string.state_folder_open else R.string.state_folder_closed),
        badgeState(row.badge),
    ).joinToString(", ")
    Row(
        modifier
            .fillMaxWidth()
            .expressivePress(interactions, PressMorph.squarer(HomeCardCorner))
            .instantPress(interactions)
            .combinedClickable(
                interactionSource = interactions,
                indication = ripple(),
                onClickLabel = stringResource(if (open) R.string.action_close_folder else R.string.action_open_folder),
                onLongClickLabel = stringResource(R.string.action_folder_options),
                // Felt as well: the long click plays its own haptic.
                onLongClick = onOptions,
                onClick = onToggle,
            )
            .semantics(mergeDescendants = true) { stateDescription = state }
            .heightIn(min = metrics.minHeight)
            .padding(horizontal = FavoriteRowInset, vertical = metrics.verticalPadding),
        horizontalArrangement = Arrangement.spacedBy(FavoriteIconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FolderTile(row.apps, icons, open, metrics.iconSize)
        Column(Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    row.name,
                    style = metrics.label(MaterialTheme.typography).onWallpaper(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                when (val badge = row.badge) {
                    AppBadge.None -> Unit
                    AppBadge.Dot -> NotificationDot()
                    is AppBadge.Count -> CountBadge(badge)
                }
            }
            // Closed, the folder says what is in it; open, its apps say it themselves.
            AnimatedVisibility(
                visible = !open,
                enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
                exit = shrinkVertically(motion.defaultSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
            ) {
                Text(
                    row.apps.joinToString(", ") { it.app.label },
                    style = MaterialTheme.typography.bodyMedium.onWallpaper(),
                    color = secondaryTextColor(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(
            painterResource(R.drawable.sym_keyboard_arrow_down),
            contentDescription = null,
            tint = secondaryTextColor(),
            modifier = Modifier.graphicsLayer { rotationZ = turn },
        )
    }
}

private const val HALF_TURN = 180f
