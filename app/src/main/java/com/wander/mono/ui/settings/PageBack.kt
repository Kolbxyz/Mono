package com.wander.mono.ui.settings

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import com.wander.mono.ui.BACK_GIVE
import com.wander.mono.ui.components.backResistance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * A settings page over the hub, on two clocks. [presence] is where the page is (0 to 1, briefly
 * past either end on a spatial spring) and only ever moves it: its scale into the card it opens
 * out of and leaves as ([morph]). [opacity] is how much of it is seen, on an effects spring that
 * never overshoots, and drives everything that must not wobble: the page's alpha and how far the
 * hub sits behind it ([depth]). A spring bouncing past 0 can then never flash the leaving page or
 * its text back in, nor dim the hub again once it has come forward.
 *
 * A predictive back swipe sets [progress], leaning the page [lean] (`backLean`) as it shrinks and
 * handing the hub back up to [BACK_GIVE] of its depth, so the swipe seeks the very transition the
 * page leaves on. A committed swipe leaves [progress] where it was: the page carries on from where
 * the finger took it, with no jump on release; an abandoned one settles back to whole.
 */
@Stable
internal class PageBack(open: Boolean, private val scope: CoroutineScope) {
    var presence by mutableFloatStateOf(if (open) 1f else 0f)
        private set

    var opacity by mutableFloatStateOf(if (open) 1f else 0f)
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

    /** How far behind the page the hub is: as much as the page is seen, less what a swipe gives back. */
    val depth: Float
        get() = opacity.coerceIn(0f, 1f) * (1f - backResistance(progress) * BACK_GIVE)

    /** The page is whole and opaque over the hub, which then need not be drawn at all. */
    val covers: Boolean get() = opacity >= 1f && morph <= 0f

    /** Anything of the page is still on screen: it stays composed until it has faded out. */
    val visible: Boolean get() = opacity > 0f

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
     * A page opens ([open]) or leaves: its place along [spatial], its opacity along [effects],
     * together, until both get there or the caller is cancelled. Each opening starts whole,
     * whatever the last swipe left.
     */
    suspend fun follow(open: Boolean, spatial: AnimationSpec<Float>, effects: AnimationSpec<Float>) {
        if (open) {
            settle?.cancel()
            progress = 0f
            lean = 0f
        }
        val target = if (open) 1f else 0f
        coroutineScope {
            launch { animate(opacity, target, animationSpec = effects) { value, _ -> opacity = value } }
            animate(presence, target, animationSpec = spatial) { value, _ -> presence = value }
        }
    }
}
