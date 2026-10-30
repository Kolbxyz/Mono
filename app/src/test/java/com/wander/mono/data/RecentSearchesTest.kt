package com.wander.mono.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecentSearchesTest {
    private fun remember(list: List<RecentSearch>, query: String, key: String) =
        RecentSearches.remembered(list, RecentSearch(query, key))

    @Test
    fun `the newest search comes first and only the last few are kept`() {
        val list = (1..10).fold(emptyList<RecentSearch>()) { acc, i -> remember(acc, "query $i", "app:$i") }
        assertEquals(RecentSearches.MAX, list.size)
        assertEquals(RecentSearch("query 10", "app:10"), list.first())
        assertEquals(RecentSearch("query 3", "app:3"), list.last())
    }

    @Test
    fun `the same query or the same result is kept once, as last opened`() {
        val maps = remember(emptyList(), "maps", "app:maps")
        assertEquals(listOf(RecentSearch("MAPS", "app:osmand")), remember(maps, "MAPS", "app:osmand"))
        assertEquals(listOf(RecentSearch("ma", "app:maps")), remember(maps, "ma", "app:maps"))
    }

    @Test
    fun `queries are kept trimmed on one line, blank ones and unstorable keys not at all`() {
        assertEquals(listOf(RecentSearch("google maps", "k")), remember(emptyList(), "  google \n\tmaps ", "k"))
        assertEquals(emptyList<RecentSearch>(), remember(emptyList(), "   ", "k"))
        assertEquals(emptyList<RecentSearch>(), remember(emptyList(), "maps", "a\tb"))
        assertEquals(emptyList<RecentSearch>(), remember(emptyList(), "maps", ""))
    }

    @Test
    fun `the result last opened for a query is preferred, whatever its case`() {
        val list = listOf(RecentSearch("Maps", "app:maps"), RecentSearch("cal", "app:calendar"))
        assertEquals("app:maps", RecentSearches.preferredFor(list, " maps "))
        assertEquals("app:calendar", RecentSearches.preferredFor(list, "CAL"))
        assertNull(RecentSearches.preferredFor(list, "ca"))
    }

    @Test
    fun `stored searches read back as written, broken lines skipped`() {
        val list = listOf(RecentSearch("maps", "app:pkg/pkg.Main/0"), RecentSearch("léa", "contact:12"))
        assertEquals(list, RecentSearches.decode(RecentSearches.encode(list)))
        assertEquals(emptyList<RecentSearch>(), RecentSearches.decode(""))
        assertEquals(list.take(1), RecentSearches.decode("garbage\n\tno key\nkey only\t\napp:pkg/pkg.Main/0\tmaps"))
    }
}
