package com.wander.mono.ui.drawer

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTyposTest {
    @Test
    fun `equal strings are within one edit`() {
        assertTrue(withinOneEdit("maps", "maps"))
        assertTrue(withinOneEdit("", ""))
    }

    @Test
    fun `one replaced, added or removed character is one edit`() {
        assertTrue(withinOneEdit("maps", "mapz"))
        assertTrue(withinOneEdit("maps", "map"))
        assertTrue(withinOneEdit("map", "maps"))
        assertTrue(withinOneEdit("spotify", "spotfy"))
        assertTrue(withinOneEdit("", "a"))
        assertTrue(withinOneEdit("xmaps", "maps"))
    }

    @Test
    fun `two swapped neighbours are one edit`() {
        assertTrue(withinOneEdit("maps", "mpas"))
        assertTrue(withinOneEdit("ab", "ba"))
        assertTrue(withinOneEdit("spotify", "sptoify"))
        assertTrue(withinOneEdit("signal", "signla"))
    }

    @Test
    fun `two edits or more are too many`() {
        assertFalse(withinOneEdit("maps", "mspa"))
        assertFalse(withinOneEdit("kitten", "sitting"))
        assertFalse(withinOneEdit("abc", ""))
        assertFalse(withinOneEdit("maps", "mapsxy"))
        assertFalse(withinOneEdit("abcd", "badc"))
        assertFalse(withinOneEdit("abc", "cab"))
    }
}
