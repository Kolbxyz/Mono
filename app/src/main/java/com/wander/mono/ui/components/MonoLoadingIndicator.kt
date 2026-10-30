package com.wander.mono.ui.components

import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.wander.mono.R
import com.wander.mono.ui.theme.LocalReducedMotion

/**
 * The Expressive loading indicator, Material's shapes morphing into one another, for the rare
 * wait Mono shows (a widget list, a backup being read). [contained] sets it on its own
 * `primaryContainer` disc, for when it floats over content rather than filling an empty space.
 *
 * Its morph and spin run on their own infinite animations, not `MaterialTheme.motionScheme`, so
 * reduced motion is honoured here: the indicator holds still on its first shape, a busy-looking
 * soft burst, instead of spinning. Announced as "Loading" to screen readers.
 *
 * Adopters: `ui/widgets/WidgetPickerSheet.kt` (its bare `LoadingIndicator()`).
 */
@Composable
fun MonoLoadingIndicator(modifier: Modifier = Modifier, contained: Boolean = false) {
    val label = stringResource(R.string.loading)
    val described = modifier.semantics { contentDescription = label }
    val polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
    val still = LocalReducedMotion.current
    when {
        // A determinate indicator at zero progress is the first polygon, unrotated: a still frame.
        still && contained -> ContainedLoadingIndicator(progress = { 0f }, modifier = described, polygons = polygons)
        still -> LoadingIndicator(progress = { 0f }, modifier = described, polygons = polygons)
        contained -> ContainedLoadingIndicator(modifier = described)
        else -> LoadingIndicator(modifier = described)
    }
}
