package com.wander.mono.ui.drawer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * Whether the rail shows only its current letter: while the list moves ([scrolling]) or a finger
 * scrubs the rail, and for [LINGER_MS] after, so a run of flings does not blink the alphabet back
 * in between them. At rest the whole alphabet shows.
 */
@Composable
internal fun rememberRailFocus(scrolling: Boolean, scrubbing: Boolean): State<Boolean> {
    val focused = remember { mutableStateOf(false) }
    val moving = scrolling || scrubbing
    LaunchedEffect(moving) {
        if (moving) {
            focused.value = true
        } else {
            delay(LINGER_MS)
            focused.value = false
        }
    }
    return focused
}

/**
 * How visible the letter at [index] is: fully when the rail is not [focused], else only the
 * [focus] letter (the one under the finger, or the list's current section), the rest fading out.
 */
@Composable
internal fun railLetterAlpha(index: Int, focus: Int, focused: Boolean): Float {
    val target = when {
        !focused -> 1f
        index == focus -> 1f
        else -> 0f
    }
    val alpha by animateFloatAsState(target, MaterialTheme.motionScheme.fastEffectsSpec())
    return alpha
}

/** How long the rail keeps only its current letter after the list stops. */
private const val LINGER_MS = 700L
