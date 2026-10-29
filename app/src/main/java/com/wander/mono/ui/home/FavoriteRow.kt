package com.wander.mono.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.components.AppIcon
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.AppLabel

/**
 * A favourite: its icon (none in text-only mode) and its name in large rounded type, at least
 * 56 dp tall (spec 4.3). Its taps, holds and drags are handled around it, by `ReorderableFavorite`.
 */
@Composable
fun FavoriteRow(row: HomeRow.App, icons: AppIcons, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(row.app.key, icons, size = ICON_SIZE)
        AppLabel(
            app = row.app,
            badge = row.badge,
            style = MaterialTheme.typography.headlineSmall.onWallpaper(),
        )
    }
}

private val ICON_SIZE = 36.dp
