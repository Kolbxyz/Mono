package com.wander.mono.ui.theme

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * [LazyItemScope.animateItem] with every spec taken from `MaterialTheme.motionScheme`, so lazy
 * lists follow the expressive motion and [NoMotionScheme] when motion is reduced. The bare
 * `animateItem()` falls back to foundation's own springs, which neither does.
 */
@Composable
fun LazyItemScope.schemeAnimateItem(): Modifier {
    val motion = MaterialTheme.motionScheme
    return Modifier.animateItem(
        fadeInSpec = motion.defaultEffectsSpec(),
        placementSpec = motion.defaultSpatialSpec(),
        fadeOutSpec = motion.fastEffectsSpec(),
    )
}
