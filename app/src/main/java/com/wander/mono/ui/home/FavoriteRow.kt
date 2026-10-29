package com.wander.mono.ui.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.components.AppLabel

/**
 * A favourite: just its name in large type, at least 56 dp tall (spec 4.3). Its taps, holds and
 * drags are handled around it, by `ReorderableFavorite`.
 */
@Composable
fun FavoriteRow(row: HomeRow.App, modifier: Modifier = Modifier) {
    AppLabel(
        app = row.app,
        badge = row.badge,
        style = MaterialTheme.typography.headlineSmall.onWallpaper(),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .padding(vertical = 8.dp),
    )
}
