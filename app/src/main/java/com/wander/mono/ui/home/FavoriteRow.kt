package com.wander.mono.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.components.AppBadge
import com.wander.mono.ui.components.AppIcon
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.AppLabel
import com.wander.mono.ui.theme.textAlpha

/**
 * A favourite: its icon (none in text-only mode) and its name in large rounded type, as tall and
 * as large as the user's row density makes it (spec 4.3). Its taps, holds and drags are handled
 * around it, by `ReorderableFavorite`. Past its daily limit ([overLimit]) it fades to half (not
 * with high-contrast text), and TalkBack says so.
 */
@Composable
fun FavoriteRow(row: HomeRow.App, icons: AppIcons, modifier: Modifier = Modifier, overLimit: Boolean = false) {
    val metrics = favoriteRowMetrics(LocalRowDensity.current)
    val alpha by animateFloatAsState(
        if (overLimit) textAlpha(OVER_LIMIT_ALPHA) else 1f,
        MaterialTheme.motionScheme.defaultEffectsSpec(),
    )
    // Set here, the state would hide the label's badge state from TalkBack: both go together.
    val limitState = if (overLimit) {
        listOfNotNull(stringResource(R.string.favorite_limit_reached), badgeState(row.badge)).joinToString(", ")
    } else {
        null
    }
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = metrics.minHeight)
            .padding(horizontal = 4.dp, vertical = metrics.verticalPadding)
            .graphicsLayer { this.alpha = alpha }
            .then(if (limitState != null) Modifier.semantics { stateDescription = limitState } else Modifier),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(row.app.key, icons, size = metrics.iconSize)
        AppLabel(
            app = row.app,
            badge = row.badge,
            style = metrics.label(MaterialTheme.typography).onWallpaper(),
        )
    }
}

/** What a label announces for [badge], repeated where a row's own state would hide it. */
@Composable
internal fun badgeState(badge: AppBadge): String? = when (badge) {
    AppBadge.None -> null
    AppBadge.Dot -> stringResource(R.string.state_has_notifications)
    is AppBadge.Count -> pluralStringResource(R.plurals.state_notification_count, badge.count, badge.count)
}

/** How far a favourite past its daily limit fades: still readable, plainly set back. */
private const val OVER_LIMIT_ALPHA = 0.5f
