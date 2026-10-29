package com.wander.mono.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ListMovesTest {
    @Test
    fun `moves by the given places and clamps at both ends`() {
        val list = listOf("a", "b", "c")
        assertEquals(listOf("b", "a", "c"), list.moved("b", -1))
        assertEquals(listOf("a", "c", "b"), list.moved("b", 1))
        assertEquals(listOf("b", "c", "a"), list.moved("a", 5))
        assertEquals(list, list.moved("z", 1))
    }

    @Test
    fun `reorders the given items in their own places and leaves the rest`() {
        val list = listOf("a", "gone", "b", "c")
        assertEquals(listOf("c", "gone", "a", "b"), list.reordered(listOf("c", "a", "b")))
    }

    @Test
    fun `reordering ignores unknown items and keeps unlisted ones`() {
        val list = listOf("a", "b", "c")
        assertEquals(listOf("b", "a", "c"), list.reordered(listOf("b", "z", "a")))
        assertEquals(list, list.reordered(emptyList()))
    }
}
