package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.contacts.Contact
import com.wander.mono.core.system.SettingsPage
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchLayoutTest {
    private fun app(label: String) = SearchHit.App(
        DrawerEntry(LaunchableApp(AppKey(label, "$label.Main", 0), label, isMainProfile = true), AppBadge.None),
    )

    private fun shortcut(label: String) = SearchHit.Shortcut(AppShortcut("pkg", label, 0, label), owner = null)

    private fun person(name: String) = SearchHit.Person(Contact(name.length.toLong(), name, name))

    private fun setting(label: String) = SearchHit.Setting(SettingsPage(label, label, emptyList(), action = null))

    @Test
    fun `nothing to lay out without results`() {
        val layout = SearchLayout.of(emptyList())
        assertNull(layout.top)
        assertEquals(emptyList<SearchHit.App>(), layout.apps)
        assertEquals(emptyList<ResultGroup>(), layout.groups)
    }

    @Test
    fun `top hit stands alone and the other apps form one row`() {
        val results = listOf(app("Maps"), app("Mail"), shortcut("Map home"), app("Music"))
        val layout = SearchLayout.of(results)
        assertEquals(results[0], layout.top)
        assertEquals(listOf(results[1], results[3]), layout.apps)
        assertEquals(listOf(ResultGroup(ResultKind.Shortcuts, listOf(results[2]))), layout.groups)
    }

    @Test
    fun `groups follow the ranking and keep their hits in order`() {
        val calc = SearchHit.Calculation("4")
        val results = listOf(calc, shortcut("b"), person("Bea"), shortcut("a"), setting("Bluetooth"))
        val layout = SearchLayout.of(results)
        assertEquals(calc, layout.top)
        val kinds = layout.groups.map { it.kind }
        assertEquals(listOf(ResultKind.Shortcuts, ResultKind.Contacts, ResultKind.Settings), kinds)
        assertEquals(listOf(results[1], results[3]), layout.groups[0].hits)
    }

    @Test
    fun `opening and sharing pasted text are one group`() {
        val link = SearchHit.Link("https://maps.org")
        val results = listOf(app("Maps"), link, person("Mia"), SearchHit.ShareText("maps.org"))
        val layout = SearchLayout.of(results)
        assertEquals(listOf(ResultKind.HandOff, ResultKind.Contacts), layout.groups.map { it.kind })
        assertEquals(listOf(results[1], results[3]), layout.groups[0].hits)
    }

    @Test
    fun `a shortcut on top is not repeated in its group`() {
        val results = listOf(shortcut("Call mum"), shortcut("Call dad"))
        val layout = SearchLayout.of(results)
        assertEquals(results[0], layout.top)
        assertEquals(listOf(results[1]), layout.groups.single().hits)
    }
}
