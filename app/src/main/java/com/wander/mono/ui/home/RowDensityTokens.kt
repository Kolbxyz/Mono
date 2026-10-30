package com.wander.mono.ui.home

import androidx.annotation.StringRes
import androidx.compose.runtime.staticCompositionLocalOf
import com.wander.mono.R
import com.wander.mono.data.settings.RowDensity

@StringRes
fun densityLabel(density: RowDensity): Int = when (density) {
    RowDensity.Compact -> R.string.density_compact
    RowDensity.Default -> R.string.density_default
    RowDensity.Comfortable -> R.string.density_comfortable
}

/** The user's row density, provided by home for its favourite rows. */
internal val LocalRowDensity = staticCompositionLocalOf { RowDensity.Default }
