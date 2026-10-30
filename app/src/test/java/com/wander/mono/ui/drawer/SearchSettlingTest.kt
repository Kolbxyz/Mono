package com.wander.mono.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchSettlingTest {
    private val ada = QueryResults("ada", listOf("Ada Lovelace"))
    private val ad = QueryResults("ad", listOf("Ada Lovelace", "Adele"))

    @Test
    fun `source results count only for the query they answer, spaces aside`() {
        assertEquals(listOf("Ada Lovelace"), ada.itemsFor("ada"))
        assertEquals(listOf("Ada Lovelace"), ada.itemsFor(" ada "))
        assertTrue(ada.itemsFor("adal").isEmpty())
    }

    @Test
    fun `results are built for the typed query only once every source has answered it`() {
        assertEquals("ada", builtFor("ada", ada, QueryResults("ada", emptyList<String>())))
        assertNull(builtFor("ada", ada, ad))
        assertEquals("ada ", builtFor("ada ", ada))
    }

    private val first = listOf<SearchHit>(SearchHit.Calculation("4"))
    private val second = listOf<SearchHit>(SearchHit.Web("ada", "Vanadium"))

    @Test
    fun `a keystroke keeps the last results on screen until the new ones are in`() {
        val shown = ShownResults().next("2+2", builtFor = "2+2", latest = first)
        assertEquals(ShownResults(first, settled = true), shown)

        val waiting = shown.next("2+2=", builtFor = null, latest = emptyList())
        assertEquals(first, waiting.results)
        assertFalse(waiting.settled)

        val stale = waiting.next("ada", builtFor = "2+2", latest = first)
        assertEquals(first, stale.results)
        assertFalse(stale.settled)

        assertEquals(ShownResults(second, settled = true), stale.next("ada", builtFor = "ada", latest = second))
    }

    @Test
    fun `nothing found is settled only for the query typed`() {
        val none = ShownResults(first, settled = true).next("zzz", builtFor = "zzz", latest = emptyList())
        assertTrue(none.results.isEmpty())
        assertTrue(none.settled)
    }

    @Test
    fun `a cleared query keeps what was shown while the results fade out`() {
        val shown = ShownResults(first, settled = true)
        assertEquals(shown, shown.next("", builtFor = "", latest = emptyList()))
        assertEquals(shown, shown.next("  ", builtFor = null, latest = emptyList()))
    }
}
