package com.wander.mono.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CommandQueryTest {
    @Test
    fun `a note keeps its text as typed`() {
        assertEquals("buy milk", CommandQuery.note("note buy milk"))
        assertEquals("Buy Milk", CommandQuery.note("  Note:  Buy Milk "))
        assertEquals("call the bank", CommandQuery.note("take a note call the bank"))
        assertEquals("acheter du pain", CommandQuery.note("noter acheter du pain"))
        assertEquals("Été à Lyon", CommandQuery.note("prendre une note Été à Lyon"))
    }

    @Test
    fun `not notes`() {
        assertNull(CommandQuery.note("note"))
        assertNull(CommandQuery.note("note   "))
        assertNull(CommandQuery.note("notepad"))
        assertNull(CommandQuery.note("buy milk"))
    }

    @Test
    fun `whom to call, in English or French, accents or not`() {
        assertEquals("Anna", CommandQuery.callee("call Anna"))
        assertEquals("Anna Petit", CommandQuery.callee("Appeler Anna Petit"))
        assertEquals("Hélène", CommandQuery.callee("téléphoner à Hélène"))
        assertEquals("Hélène", CommandQuery.callee("telephoner a Hélène"))
        assertEquals("maman", CommandQuery.callee("appelle maman"))
        assertNull(CommandQuery.callee("call"))
        assertNull(CommandQuery.callee("calligraphy"))
        assertNull(CommandQuery.callee("Anna"))
    }

    @Test
    fun `a typed number is called as typed`() {
        assertEquals("06 12 34 56 78", CommandQuery.callee("call 06 12 34 56 78")?.let(CommandQuery::number))
        assertEquals("+33 6 12 34 56 78", CommandQuery.number("+33 6 12 34 56 78"))
        assertEquals("112", CommandQuery.number("112"))
        assertNull(CommandQuery.number("12"))
        assertNull(CommandQuery.number("Anna 2"))
    }

    @Test
    fun `accents fold one letter for one letter`() {
        assertEquals("reveil a l'ete", withoutAccents("réveil à l'été"))
        assertEquals("Ca", withoutAccents("Ça"))
    }
}
