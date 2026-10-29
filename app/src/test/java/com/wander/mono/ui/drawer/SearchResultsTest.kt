package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.contacts.Contact
import com.wander.mono.core.siblings.WandaTrack
import com.wander.mono.core.system.SettingsPage
import com.wander.mono.core.system.WebSearchTarget
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchResultsTest {
    private fun entry(label: String) = label.lowercase().replace(" ", ".").let { pkg ->
        DrawerEntry(LaunchableApp(AppKey(pkg, "$pkg.Main", 0), label, isMainProfile = true), AppBadge.None)
    }

    private fun page(label: String, vararg keywords: String) =
        SettingsPage(label.lowercase(), label, keywords.toList(), action = "android.settings.$label")

    private val browser = WebSearchTarget("Vanadium")

    private fun search(
        query: String,
        apps: List<DrawerEntry> = emptyList(),
        shortcuts: List<AppShortcut> = emptyList(),
        contacts: List<Contact> = emptyList(),
        tracks: List<WandaTrack> = emptyList(),
        pages: List<SettingsPage> = emptyList(),
        web: WebSearchTarget? = browser,
    ) = SearchResults.assemble(query, apps, shortcuts, contacts, tracks, pages) { web }

    private fun track(title: String) = WandaTrack(title, title, null, "wanda://track?id=$title", "com.wander.android")

    @Test
    fun `wanda songs come after contacts and before settings, in wanda's order, capped`() {
        val songs = listOf(track("Song b"), track("Song a"), track("Song c"), track("Song d"))
        val hits = search(
            "song",
            contacts = listOf(Contact(1, "k", "Song Person")),
            tracks = songs,
            pages = listOf(page("Song settings")),
        )
        assertEquals(
            listOf(SearchHit.Person::class) + List(SearchResults.MAX_TRACKS) { SearchHit.Track::class } +
                SearchHit.Setting::class,
            hits.map { it::class },
        )
        assertEquals(songs.take(SearchResults.MAX_TRACKS), hits.filterIsInstance<SearchHit.Track>().map { it.track })
    }

    @Test
    fun `a wanda song is a match, so the web is not offered`() {
        assertEquals(listOf(SearchHit.Track::class), search("xyz", tracks = listOf(track("Xyz"))).map { it::class })
    }

    @Test
    fun `groups come in order calculator, apps, shortcuts, contacts, settings`() {
        val hits = search(
            "2+2",
            apps = listOf(entry("App 2+2")),
            shortcuts = listOf(AppShortcut("pkg", "id", 0, "Shortcut 2+2")),
            contacts = listOf(Contact(1, "k", "Contact 2+2")),
            pages = listOf(page("Page 2+2")),
        )
        assertEquals(
            listOf(
                SearchHit.Calculation::class,
                SearchHit.App::class,
                SearchHit.Shortcut::class,
                SearchHit.Person::class,
                SearchHit.Setting::class,
            ),
            hits.map { it::class },
        )
        assertEquals("4", (hits.first() as SearchHit.Calculation).result)
    }

    @Test
    fun `the best app comes first even when a shortcut matches better`() {
        val hits = search(
            "cam",
            apps = listOf(entry("Scam Alert"), entry("Camera")),
            shortcuts = listOf(AppShortcut("pkg", "id", 0, "Cam")),
        )
        assertEquals(listOf("Camera", "Scam Alert", "Cam"), hits.map { it.label })
    }

    @Test
    fun `settings pages match by label or keyword with the same ranking`() {
        val pages = listOf(page("Wi‑Fi", "wifi", "internet"), page("Display", "brightness"), page("Battery"))
        assertEquals(listOf("Wi‑Fi"), search("wifi", pages = pages).map { it.label })
        assertEquals(listOf("Display"), search("bright", pages = pages).map { it.label })
        assertEquals(listOf("Battery"), search("bat", pages = pages).map { it.label })
    }

    @Test
    fun `contacts are ranked by name and capped`() {
        val contacts = (1..8).map { Contact(it.toLong(), "k$it", "Anna $it") } + Contact(9, "k9", "Bob")
        val hits = search("anna", contacts = contacts)
        assertEquals(SearchResults.MAX_CONTACTS, hits.size)
        assertTrue(hits.all { it is SearchHit.Person && it.label.startsWith("Anna") })
    }

    @Test
    fun `contacts the provider matched on something other than the name are left out`() {
        assertTrue(search("zed", contacts = listOf(Contact(1, "k", "Anna")), web = null).isEmpty())
    }

    @Test
    fun `web search is offered only when nothing else matches`() {
        assertEquals(listOf(SearchHit.Web("weather paris", "Vanadium")), search(" weather paris "))
        assertTrue(search("maps", apps = listOf(entry("Maps"))).none { it is SearchHit.Web })
        assertTrue(search("1+1").none { it is SearchHit.Web })
    }

    @Test
    fun `no web search without a browser, and nothing for a blank query`() {
        assertTrue(search("weather", web = null).isEmpty())
        assertTrue(search("   ").isEmpty())
    }
}
