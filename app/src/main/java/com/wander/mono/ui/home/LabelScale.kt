package com.wander.mono.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * [content] with its text grown by [scale] on top of the user's font size; dp measures stay put,
 * so rows grow only as far as their text needs. At 1 the system's own density (and Android 14's
 * non-linear font scaling) is passed through untouched.
 */
@Composable
internal fun ScaledLabels(scale: Float, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val scaled = if (scale == 1f) density else Density(density.density, density.fontScale * scale)
    CompositionLocalProvider(LocalDensity provides scaled, content = content)
}
