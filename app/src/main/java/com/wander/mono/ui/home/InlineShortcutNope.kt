package com.wander.mono.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.components.MonoHaptics
import com.wander.mono.ui.components.rememberMonoHaptics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A swipe right on a favourite with no shortcuts: the row gives a little the way it was pushed and
 * springs back, with the reject haptic, so it reads as "nothing here". Under reduced motion only
 * the haptic is left.
 */
@Stable
class InlineShortcutNope internal constructor(
    private val scope: CoroutineScope,
    private val haptics: MonoHaptics,
    private val motion: MotionScheme,
    private val distancePx: Float,
) {
    private val offset = Animatable(0f)

    /** On the favourite's row, not its chips. */
    val modifier: Modifier = Modifier.graphicsLayer { translationX = offset.value }

    fun play() {
        haptics.reject()
        scope.launch {
            offset.animateTo(distancePx, motion.fastSpatialSpec())
            offset.animateTo(0f, motion.defaultSpatialSpec())
        }
    }
}

@Composable
fun rememberInlineShortcutNope(): InlineShortcutNope {
    val scope = rememberCoroutineScope()
    val haptics = rememberMonoHaptics()
    val motion = MaterialTheme.motionScheme
    val distancePx = with(LocalDensity.current) { NopeDistance.toPx() }
    return remember(scope, haptics, motion, distancePx) { InlineShortcutNope(scope, haptics, motion, distancePx) }
}

/** How far the row gives before springing back. */
private val NopeDistance = 12.dp
