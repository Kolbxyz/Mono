package com.wander.mono.ui.home

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.components.AppLabel

/**
 * A favourite: just its name in large type, at least 56 dp tall (spec 4.3). Tap opens it,
 * long-press opens its options.
 */
@Composable
fun FavoriteRow(
    row: HomeRow.App,
    onLaunch: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppLabel(
        app = row.app,
        hasNotifications = row.hasNotifications,
        style = MaterialTheme.typography.headlineSmall.onWallpaper(),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClickLabel = stringResource(R.string.action_open),
                onLongClickLabel = stringResource(R.string.action_app_options),
                onLongClick = onLongPress,
                onClick = onLaunch,
            )
            .wrapContentHeight(Alignment.CenterVertically)
            .padding(vertical = 8.dp),
    )
}
