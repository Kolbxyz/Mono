package com.wander.mono.core.media

import org.junit.Assert.assertEquals
import org.junit.Test

class QueueEntryTest {
    private val queue = (0L..5L).map { QueueEntry(it, "Track $it", null) }

    @Test
    fun `up next is the few items after the playing one`() {
        assertEquals(listOf(2L, 3L, 4L), upNext(queue, activeId = 1L).map { it.id })
    }

    @Test
    fun `near the end of the queue only what is left is shown`() {
        assertEquals(listOf(5L), upNext(queue, activeId = 4L).map { it.id })
        assertEquals(emptyList<Long>(), upNext(queue, activeId = 5L).map { it.id })
    }

    @Test
    fun `an unknown or missing active item guesses nothing`() {
        assertEquals(emptyList<QueueEntry>(), upNext(queue, activeId = -1L))
        assertEquals(emptyList<QueueEntry>(), upNext(queue, activeId = 42L))
    }

    @Test
    fun `the count is capped`() {
        assertEquals(listOf(1L, 2L), upNext(queue, activeId = 0L, max = 2).map { it.id })
    }
}
