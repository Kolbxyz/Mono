package com.wander.mono.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Whether high-contrast text is on (Mono's setting or Android's). Home provides it for its rows. */
val LocalHighContrast = staticCompositionLocalOf { false }

/** The colour for secondary lines: `onSurfaceVariant`, or full `onSurface` with high contrast. */
@Composable
@ReadOnlyComposable
fun secondaryTextColor(): Color =
    if (LocalHighContrast.current) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant

/** [alpha] for dimmed text, or fully opaque with high contrast: translucent text is the first to blur. */
@Composable
@ReadOnlyComposable
fun textAlpha(alpha: Float): Float = if (LocalHighContrast.current) 1f else alpha
