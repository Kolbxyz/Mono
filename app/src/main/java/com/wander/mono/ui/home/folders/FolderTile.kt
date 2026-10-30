package com.wander.mono.ui.home.folders

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import com.wander.mono.data.settings.IconMode
import com.wander.mono.ui.components.AppIcon
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.home.HomeRow

/**
 * A folder's picture, in the slot an app icon takes: up to four of its apps' icons, small, on a
 * secondary-container tile whose corners round out to a circle while the folder is open. Nothing
 * in text-only mode, like an app icon. Decorative: the row names the folder.
 */
@Composable
internal fun FolderTile(apps: List<HomeRow.App>, icons: AppIcons, open: Boolean, size: Dp) {
    if (icons.mode == IconMode.TextOnly) return
    val corner by animateFloatAsState(
        if (open) ROUND_PERCENT else SQUARE_PERCENT,
        MaterialTheme.motionScheme.defaultSpatialSpec(),
    )
    val mini = size * MINI_FRACTION
    val gap = size * GAP_FRACTION
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(percent = corner.toInt()))
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(gap), horizontalAlignment = Alignment.CenterHorizontally) {
            apps.take(PREVIEWED).chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    pair.forEach { AppIcon(it.app.key, icons, size = mini) }
                }
            }
        }
    }
}

private const val PREVIEWED = 4
private const val MINI_FRACTION = 0.36f
private const val GAP_FRACTION = 0.06f
private const val SQUARE_PERCENT = 30f
private const val ROUND_PERCENT = 50f
