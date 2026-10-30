package com.wander.mono.ui.home.live

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.notifications.LiveActivity
import com.wander.mono.ui.theme.secondaryTextColor

/**
 * The next turn, readable at arm's length: the app's turn arrow on a large tonal tile, the
 * distance (the notification's title) as a headline and the street (its text) beneath. An app
 * that draws no arrow gets the plain navigation symbol rather than a made-up turn.
 */
@Composable
internal fun LiveNavigationContent(activity: LiveActivity) {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(TILE).clip(MaterialTheme.shapes.large).background(scheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            val icon = activity.icon
            if (icon != null) {
                Image(
                    icon.image,
                    contentDescription = stringResource(R.string.live_next_turn),
                    colorFilter = if (icon.tintable) ColorFilter.tint(scheme.onPrimaryContainer) else null,
                    modifier = Modifier.size(ARROW),
                )
            } else {
                Icon(
                    painterResource(R.drawable.sym_navigation),
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(ARROW),
                )
            }
        }
        Column(Modifier.weight(1f)) {
            activity.title?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            activity.text?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = secondaryTextColor(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private val GAP = 16.dp
private val TILE = 64.dp

/** The arrow's size; the publisher renders the bitmap at this size too (`LivePublisher.TURN_ICON_DP`). */
private val ARROW = 40.dp
