package com.wander.mono.ui.home

import androidx.annotation.StringRes
import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.R
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
        minHeight = 48.dp,
        verticalPadding = 2.dp,
        iconSize = 32.dp,
        label = { it.titleLarge },
    )
    RowDensity.Default -> FavoriteRowMetrics(
        minHeight = 56.dp,
        verticalPadding = 6.dp,
        iconSize = 36.dp,
        label = { it.headlineSmall },
    )
    RowDensity.Comfortable -> FavoriteRowMetrics(
        minHeight = 64.dp,
        verticalPadding = 10.dp,
        iconSize = 40.dp,
        label = { it.headlineMedium },
    )
}

@StringRes
fun densityLabel(density: RowDensity): Int = when (density) {
    RowDensity.Compact -> R.string.density_compact
    RowDensity.Default -> R.string.density_default
    RowDensity.Comfortable -> R.string.density_comfortable
}

/** The user's row density, provided by home for its favourite rows. */
internal val LocalRowDensity = staticCompositionLocalOf { RowDensity.Default }
