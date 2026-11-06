package com.wander.mono.ui.drawer

import com.wander.mono.ui.components.rubberBand
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos

/**
 * Geometry of the letter rail. The letters' column, [top] down the rail and [height] tall, is split
 * into one equal slot per letter, a letter drawn at the centre of its slot; a finger at [y] (from the
 * rail's top) is over the slot it falls in. [RailEntries] says which list item each entry stands for.
 */
internal object RailMath {
    /**
     * How many letters on each side of the finger the wave reaches before it is flat again: wide, so
     * the rail bends as one long curve toward the thumb rather than a bump under it.
     */
    const val WAVE_REACH = 9f

    /**
     * The letter under a finger at [y] over letters from [top] down, [height] tall; clamped, so
     * sliding past an end keeps the end letter.
     */
    fun letterAt(y: Float, top: Float, height: Float, count: Int): Int {
        if (count <= 0 || height <= 0f) return -1
        return ((y - top) / height * count).toInt().coerceIn(0, count - 1)
    }

    /** The finger's position in letter units: 0f is the centre of the first letter, 1f of the second. */
    fun fingerSlot(y: Float, top: Float, height: Float, count: Int): Float =
        if (count <= 0 || height <= 0f) 0f else (y - top) / height * count - HALF

    /**
     * Where a bubble [size] tall beside a finger at [y] has its top: centred on the finger, but kept
     * on a rail [railHeight] tall, so it never slides off either end of the screen.
     */
    fun bubbleTop(y: Float, size: Float, railHeight: Float): Float =
        (y - size / 2).coerceAtMost(railHeight - size).coerceAtLeast(0f)

    /**
     * How far a letter [distance] slots from the finger bows out, from 1 under the finger to 0 at
     * [WAVE_REACH] slots and beyond: a raised cosine, so the wave has no corner at its peak or feet.
     */
    fun waveFraction(distance: Float): Float {
        val d = abs(distance)
        return if (d >= WAVE_REACH) 0f else ((cos(d / WAVE_REACH * PI) + 1.0) / 2.0).toFloat()
    }

    /**
     * How far the finger has slid off the rail toward the list since it touched down at [downX]:
     * leftward in a left-to-right layout, rightward when [mirror] is -1 (right-to-left). Sliding back
     * toward the screen edge never makes it negative.
     */
    fun travel(downX: Float, x: Float, mirror: Float): Float = ((downX - x) * mirror).coerceAtLeast(0f)

    /**
     * How far the letter under the finger bows out for a finger [travel] off the rail: it follows the
     * finger one to one at first, then stiffens and never passes [limit], so the wave stays in reach
     * of the bubble however far the finger goes.
     */
    fun bow(travel: Float, limit: Float): Float = rubberBand(travel.coerceAtLeast(0f), limit)

    /** How far the letter at [index] moves out when the one under the finger, at [slot], bows out by [bow]. */
    fun letterOffset(index: Int, slot: Float, bow: Float): Float = bow * waveFraction(index - slot)

    /**
     * How much of the letter [distance] slots from the finger shows while a rail appears or leaves
     * at [reveal] (0 gone, 1 fully shown): the letters near the finger come first and the far ones
     * follow, up to [REVEAL_LAG] of the reveal later, so the rail seems to grow out of the thumb.
     */
    fun revealAlpha(distance: Float, reveal: Float): Float {
        val far = (abs(distance) / REVEAL_REACH).coerceAtMost(1f)
        return ((reveal - far * REVEAL_LAG) / (1f - REVEAL_LAG)).coerceIn(0f, 1f)
    }

    /**
     * Every how many letters one is drawn so each drawn letter gets at least [minSlot]: 1 on a
     * phone in portrait, more on a short rail (landscape), where the skipped ones still scrub.
     */
    fun labelStride(height: Float, count: Int, minSlot: Float): Int {
        if (count <= 0 || height <= 0f) return 1
        return ceil(minSlot * count / height).toInt().coerceAtLeast(1)
    }

    /**
     * Which way the list lies from the rail, in screen pixels: 1f when the rail is on the right (the
     * end edge left to right, the start edge right to left), so letters bow out leftward; -1f on the left.
     */
    fun mirror(rtl: Boolean, atStart: Boolean): Float = if (rtl == atStart) 1f else -1f

    /**
     * Where a bubble [size] wide sits across from the rail's edge nearest the list, as a screen x
     * offset: [gap] clear of the letter under the finger, which has bowed out by [bow], on the list's
     * side ([mirror] as above).
     */
    fun bubbleX(mirror: Float, gap: Float, size: Float, bow: Float): Float = -mirror * (gap + size + bow)

    /** Letters this far from the finger and beyond appear last. */
    private const val REVEAL_REACH = 14f

    /** The share of a reveal the furthest letters wait before they start to show. */
    private const val REVEAL_LAG = 0.5f

    private const val HALF = 0.5f
}
