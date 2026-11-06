package com.wander.mono.ui.drawer

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * A finger on the letter rail, wherever it comes from: the rail's own touch, or a scrub begun on
 * home's edge ([RailScrub]). Positions are rail-local pixels. [press] lands it, [move] follows it,
 * [lift] lets go; in between it lights the entry under the finger, ticks and selects each new one,
 * and bows the letters out as the finger slides toward the list. What it reads from the rail (the
 * entries, their column, the callbacks) is refreshed on every composition by [rememberRailFinger].
 * The wave and the bubble sit right on [fingerY], one to one: direct manipulation, as Niagara's.
 */
@Stable
internal class RailFinger(
    private val scope: CoroutineScope,
    internal var springBack: FiniteAnimationSpec<Float>,
) {
    /** The finger's y down the rail. */
    var fingerY by mutableFloatStateOf(0f)
        private set

    /** The entry under the finger, -1 while none is. */
    var active by mutableIntStateOf(-1)
        private set

    /** Unlike [active], kept after release, so the bubble fades out still showing the last letter. */
    var bubbleLetter by mutableStateOf("")
        private set

    var scrubbing by mutableStateOf(false)
        private set

    /**
     * How far the letter under the finger bows out from the finger's slide toward the list: set
     * straight from each move so it tracks without lag, eased back to the rail on release.
     */
    var dragBow by mutableFloatStateOf(0f)
        private set

    /** The letters' own column, inside the track's padding: what a finger's y maps onto. */
    var lettersHeight by mutableIntStateOf(0)

    internal var letters: List<String> = emptyList()
    internal var lettersTop = 0f
    internal var hasLock = false
    internal var mirror = 1f
    internal var reducedMotion = false
    internal var bowLimit = 0f
    internal var onSelect: (Int) -> Unit = {}
    internal var onTick: (RailTick) -> Unit = {}

    private var downX = 0f
    private var downEntry = -1
    private var stayed = true
    private var settle: Job? = null

    /** The finger lands at [x], [y]. */
    fun press(x: Float, y: Float) {
        settle?.cancel()
        dragBow = 0f
        scrubbing = true
        downX = x
        follow(y)
        downEntry = active
        stayed = true
    }

    /** The finger, still down, is now at [x], [y]. */
    fun move(x: Float, y: Float) {
        if (follow(y)) stayed = false
        if (!reducedMotion) dragBow = RailMath.bow(RailMath.travel(downX, x, mirror), bowLimit)
    }

    /** The finger lifts; true when it was a tap on the closing lock, not a scrub through it. */
    fun lift(): Boolean {
        scrubbing = false
        val tappedLock = hasLock && stayed && downEntry == letters.lastIndex
        active = -1
        val from = dragBow
        settle = scope.launch {
            animate(from, 0f, animationSpec = springBack) { value, _ -> dragBow = value }
        }
        return tappedLock
    }

    /** Moves to the entry under [y]; true when that is another entry than before. */
    private fun follow(y: Float): Boolean {
        fingerY = y
        val count = letters.size
        val index = RailMath.letterAt(y, lettersTop, lettersHeight.toFloat(), count)
        val moved = index >= 0 && index != active
        if (moved) {
            active = index
            bubbleLetter = letters[index]
            onTick(RailEntries.tickFor(index, count, hasLock))
            onSelect(index)
        }
        return moved
    }
}

/** What [rememberRailFinger] hands the finger from the rail on each composition. */
internal class RailFingerInputs(
    val letters: List<String>,
    val lettersTop: Float,
    val hasLock: Boolean,
    val mirror: Float,
    val reducedMotion: Boolean,
    val bowLimit: Float,
    val onSelect: (Int) -> Unit,
    val onTick: (RailTick) -> Unit,
)

/** The rail's [RailFinger], kept for the rail's life, its [inputs] refreshed as the rail recomposes. */
@Composable
internal fun rememberRailFinger(inputs: RailFingerInputs): RailFinger {
    val scope = rememberCoroutineScope()
    val motion = MaterialTheme.motionScheme
    // The default spatial spring overshoots a little: the bend settles back like something elastic.
    val springBack = motion.defaultSpatialSpec<Float>()
    val finger = remember(scope) { RailFinger(scope, springBack) }
    SideEffect {
        finger.letters = inputs.letters
        finger.lettersTop = inputs.lettersTop
        finger.hasLock = inputs.hasLock
        finger.mirror = inputs.mirror
        finger.reducedMotion = inputs.reducedMotion
        finger.bowLimit = inputs.bowLimit
        finger.springBack = springBack
        finger.onSelect = inputs.onSelect
        finger.onTick = inputs.onTick
    }
    return finger
}
