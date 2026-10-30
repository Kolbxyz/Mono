package com.wander.mono.ui.settings

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import com.wander.mono.ui.components.backResistance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * A settings page over the hub: [presence], how far it is in (0 to 1, briefly past either end on
 * a spatial spring), and [progress], how far a predictive back swipe has taken it, leaning it
 * [lean] (`backLean`). The page morphs by [morph] and the hub sits [depth] behind it, so a swipe
 * seeks the same transition the page leaves on. A committed swipe leaves [progress] where it was:
 * the page leaves from where the finger took it, carrying on into its card as the hub clears.
 */
@Stable
internal class PageBack(open: Boolean, private val scope: CoroutineScope) {
    var presence by mutableFloatStateOf(if (open) 1f else 0f)
        private set

    var progress by mutableFloatStateOf(0f)
        private set

    var lean by mutableFloatStateOf(0f)
        private set

    private var settle: Job? = null

    /** How far the page has turned into its card: the swipe's share, then its leaving. */
    val morph: Float
        get() {
            val given = backResistance(progress)
            return given + (1f - given) * (1f - presence.coerceIn(0f, 1f))
        }

    /** How far behind the page the hub is: it comes forward as the swipe or the page's leaving goes. */
    val depth: Float get() = presence.coerceIn(0f, 1f) * (1f - backResistance(progress))

    /** The page is whole and opaque over the hub, which then need not be drawn at all. */
    val covers: Boolean get() = presence >= 1f && progress == 0f

    fun swipe(value: Float, towards: Float) {
        settle?.cancel()
        progress = value
        lean = towards
    }

    /** The swipe was abandoned: the page grows back to full size along [spec]. */
    fun cancelled(spec: AnimationSpec<Float>) {
        settle?.cancel()
        settle = scope.launch { animate(progress, 0f, animationSpec = spec) { value, _ -> progress = value } }
    }

    /**
     * A page opens ([open]) or leaves along [spec], until it gets there or the caller is cancelled.
     * Each opening starts whole, whatever the last swipe left.
     */
    suspend fun follow(open: Boolean, spec: AnimationSpec<Float>) {
        if (open) {
            settle?.cancel()
            progress = 0f
            lean = 0f
        }
        animate(presence, if (open) 1f else 0f, animationSpec = spec) { value, _ -> presence = value }
    }
}
