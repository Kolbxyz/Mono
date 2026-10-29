package com.wander.mono.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Settings over home (and over the drawer when opened from it, so back returns there), sliding in
 * from a quarter of the screen to the side. [animateExit] is false when it was closed while Mono
 * was not on screen: nothing to animate.
 */
@Composable
fun SettingsLayer(open: Boolean, animateExit: Boolean, content: @Composable () -> Unit) {
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = open,
        enter = slideInHorizontally(motion.defaultSpatialSpec()) { it / SLIDE_FRACTION } +
            fadeIn(motion.defaultEffectsSpec()),
        exit = if (animateExit) {
            slideOutHorizontally(motion.fastSpatialSpec()) { it / SLIDE_FRACTION } + fadeOut(motion.fastEffectsSpec())
        } else {
            ExitTransition.None
        },
    ) {
        content()
    }
}

private const val SLIDE_FRACTION = 4
