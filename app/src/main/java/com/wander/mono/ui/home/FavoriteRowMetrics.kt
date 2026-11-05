package com.wander.mono.ui.home

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.data.settings.RowDensity

/** A favourite row's measures at one [RowDensity] (spec 4.3), all in one place. */
internal class FavoriteRowMetrics(
    val minHeight: Dp,
    val verticalPadding: Dp,
    val iconSize: Dp,
    val label: (Typography) -> TextStyle,
)

internal fun favoriteRowMetrics(density: RowDensity): FavoriteRowMetrics = when (density) {
    RowDensity.Compact -> FavoriteRowMetrics(
        minHeight = 52.dp,
        verticalPadding = 4.dp,
        iconSize = 34.dp,
        label = { it.titleLarge },
    )
    RowDensity.Default -> FavoriteRowMetrics(
        minHeight = 60.dp,
        verticalPadding = 8.dp,
        iconSize = 40.dp,
        label = { it.headlineSmall },
    )
    RowDensity.Comfortable -> FavoriteRowMetrics(
        minHeight = 68.dp,
        verticalPadding = 12.dp,
        iconSize = 44.dp,
        label = { it.headlineMedium },
    )
}

/** A favourite row's inset on both sides, inside the list's gutter. */
internal val FavoriteRowInset = 4.dp

/** The space between a favourite's icon and its name: wide, so names read as a column of their own. */
internal val FavoriteIconGap = 24.dp

/**
 * Where a favourite's name starts, so what shows under it (its notification preview) lines up with
 * the name rather than the icon. With no icons ([iconsShown] false) names start at the row's inset.
 */
internal fun FavoriteRowMetrics.labelStart(iconsShown: Boolean): Dp =
    if (iconsShown) FavoriteRowInset + iconSize + FavoriteIconGap else FavoriteRowInset
