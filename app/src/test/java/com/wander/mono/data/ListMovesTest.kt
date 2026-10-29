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
}
