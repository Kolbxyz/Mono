package com.wander.mono.ui.settings

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * How far a predictive back swipe has taken the open settings page. A committed swipe leaves it
 * where it was, so the outgoing page fades out at its shrunk size; an abandoned one settles back.
 */
@Stable
internal class PageBack(private val scope: CoroutineScope) {
    var progress by mutableFloatStateOf(0f)
        private set

    private var settle: Job? = null

    fun swipe(value: Float) {
        settle?.cancel()
        progress = value
    }

    /** The swipe was abandoned: the page grows back to full size along [spec]. */
    fun cancelled(spec: AnimationSpec<Float>) {
        settle?.cancel()
        settle = scope.launch { animate(progress, 0f, animationSpec = spec) { value, _ -> progress = value } }
    }

    /** A page is opening: it starts at full size, whatever the last one was left at. */
    fun reset() {
        settle?.cancel()
        progress = 0f
    }
}
