package com.wander.mono.ui.drawer

/**
 * The letter rail's entries against the list. The rail's first entry is ★, the top of the list;
 * the letters follow it, so rail entry `i` is the list's letter item `i - 1`, after any lead groups
 * ("New") the list has before its letters. A lock may end the rail, for the private space.
 */
internal object RailEntries {
    /**
     * The list item rail entry [railIndex] jumps to, with [lead] groups before the first letter: ★
     * goes to the very top; without lead groups the first letter does too.
     */
    fun listItemFor(railIndex: Int, lead: Int = 0): Int = if (railIndex <= 0) 0 else railIndex - 1 + lead

    /**
     * The rail entry for a list scrolled to [firstItem], [firstOffset] pixels into it: ★ at the very
     * top and over the [lead] groups, else the letter at the top of the screen; the cards after the
     * letters (work, private space) count as the last of [letters].
     */
    fun railIndexFor(firstItem: Int, firstOffset: Int, letters: Int, lead: Int = 0): Int = when {
        firstItem < lead || (firstItem == 0 && firstOffset == 0) -> 0
        else -> (firstItem - lead + 1).coerceAtMost(letters)
    }

    /** The tick for reaching rail entry [index] of [count]: a long one at ★ and at a closing lock ([hasLock]). */
    fun tickFor(index: Int, count: Int, hasLock: Boolean): RailTick =
        if (index == 0 || (hasLock && index == count - 1)) RailTick.Edge else RailTick.Letter
}

/** The haptic for reaching a rail entry: a light tick per letter, a long one at either end. */
internal enum class RailTick { Letter, Edge }
