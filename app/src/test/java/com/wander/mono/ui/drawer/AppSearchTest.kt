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
    fun `blank query and non-matches return nothing`() {
        assertTrue(AppSearch.search("  ", listOf(entry("Maps")), emptyList()).isEmpty())
        assertNull(AppSearch.score("Maps", "xyz"))
    }
}
