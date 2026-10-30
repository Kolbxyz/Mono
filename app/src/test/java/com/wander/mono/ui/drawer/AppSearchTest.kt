package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSearchTest {
    private fun entry(label: String, pkg: String = label.lowercase().replace(" ", ".")) =
        DrawerEntry(LaunchableApp(AppKey(pkg, "$pkg.Main", 0), label, isMainProfile = true), AppBadge.None)

    private fun labels(hits: List<SearchHit>) = hits.map { it.label }

    @Test
    fun `matching ignores case and accents`() {
        val hits = AppSearch.search("ecole", listOf(entry("École Directe"), entry("Calendar")), emptyList())
        assertEquals(listOf("École Directe"), labels(hits))
        assertEquals(labels(hits), labels(AppSearch.search("ÉCOLE", listOf(entry("École Directe")), emptyList())))
    }

    @Test
    fun `initials find multi-word names`() {
        val hits = AppSearch.search("ym", listOf(entry("YouTube Music"), entry("Maps")), emptyList())
        assertEquals(listOf("YouTube Music"), labels(hits))
    }

    @Test
    fun `tiers rank exact then prefix then word prefix then initials then substring`() {
        val apps = listOf(
            entry("Abacus Map"), // initials "am"
            entry("Spam"), // substring
            entry("Amaze"), // prefix
            entry("Am"), // exact
            entry("I Am Here"), // word prefix
        )
        val hits = AppSearch.search("am", apps, emptyList())
        assertEquals(listOf("Am", "Amaze", "I Am Here", "Abacus Map", "Spam"), labels(hits))
    }

    @Test
    fun `within a tier shorter names come first and apps before shortcuts`() {
        val shortcut = AppShortcut("org.phalanx.android", "!room", 0, "Camera club")
        val hits = AppSearch.search("cam", listOf(entry("Camera Pro"), entry("Camera")), listOf(shortcut))
        assertEquals(listOf("Camera", "Camera Pro", "Camera club"), labels(hits))
    }

    @Test
    fun `shortcuts are found and linked to their app`() {
        val phalanx = entry("Phalanx", "org.phalanx.android")
        val room = AppShortcut("org.phalanx.android", "!abc:example.org", 0, "Climbing friends")
        val hit = AppSearch.search("climb", listOf(phalanx), listOf(room)).single()
        assertTrue(hit is SearchHit.Shortcut)
        assertEquals(phalanx.app, (hit as SearchHit.Shortcut).owner)
    }

    @Test
    fun `multi-word queries match word prefixes in any order`() {
        val hits = AppSearch.search("mus you", listOf(entry("YouTube Music")), emptyList())
        assertEquals(listOf("YouTube Music"), labels(hits))
    }

    @Test
    fun `every tier in order, typos last`() {
        val apps = listOf(
            entry("Nite Owl"), // one typo: "nite"
            entry("Keynote"), // substring
            entry("Nice Old Tiny Editor"), // initials
            entry("Quick Note"), // word prefix
            entry("Notebook"), // prefix
            entry("Note"), // exact
        )
        val hits = AppSearch.search("note", apps, emptyList())
        assertEquals(
            listOf("Note", "Notebook", "Quick Note", "Nice Old Tiny Editor", "Keynote", "Nite Owl"),
            labels(hits),
        )
    }

    @Test
    fun `initials find google maps and play store`() {
        val apps = listOf(entry("Google Maps"), entry("Play Store"), entry("Gmail"))
        assertEquals(listOf("Gmail", "Google Maps"), labels(AppSearch.search("gm", apps, emptyList())))
        // "Google Maps" holds "ps" too, as a plain substring: after the initials.
        assertEquals(listOf("Play Store", "Google Maps"), labels(AppSearch.search("ps", apps, emptyList())))
    }

    @Test
    fun `a typo still finds the app, after every match without one`() {
        val apps = listOf(entry("Spotify"), entry("Spot Finder"), entry("Notes"))
        assertEquals(listOf("Spotify"), labels(AppSearch.search("spotfy", apps, emptyList())))
        val typoLast = listOf(entry("Nite"), entry("A Very Long Notebook App"))
        val hits = AppSearch.search("note", typoLast, emptyList())
        assertEquals(listOf("A Very Long Notebook App", "Nite"), labels(hits))
    }

    @Test
    fun `camel case words are words`() {
        assertEquals(listOf("YouTube"), labels(AppSearch.search("tube", listOf(entry("YouTube")), emptyList())))
    }

    @Test
    fun `hits carry the matched characters of their label`() {
        val shortcut = AppShortcut("pkg", "id", 0, "New École note")
        val hits = AppSearch.search("ecole", listOf(entry("École Directe")), listOf(shortcut))
        assertEquals(listOf(0..4), hits[0].matched)
        assertEquals(listOf(4..8), hits[1].matched)
    }

    @Test
    fun `an alias match ranks the item but marks nothing of its label`() {
        val ranked =
            AppSearch.rankMatched("bright", listOf("Display"), label = { it }, aliases = { listOf("brightness") })
        assertEquals(listOf(Ranked("Display", emptyList())), ranked)
    }

    @Test
    fun `blank query and non-matches return nothing`() {
        assertTrue(AppSearch.search("  ", listOf(entry("Maps")), emptyList()).isEmpty())
        assertNull(AppSearch.score("Maps", "xyz"))
    }
}
