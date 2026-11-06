package com.wander.mono.ui.home

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.data.Focus
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeContextTest {
    private val mail = app("org.mail", "Mail")
    private val chat = app("org.chat", "Chat")
    private val maps = app("org.maps", "Maps")

    private fun app(pkg: String, label: String) = LaunchableApp(AppKey(pkg, "$pkg.Main", 0), label, true)

    private fun owner(app: LaunchableApp) = PackageUser(app.key.packageName, app.key.userSerial)

    @Test
    fun `a focus strips the other badges and puts its row under the favourite`() {
        val rows = listOf(HomeRow.App(mail, AppBadge.Dot), HomeRow.App(chat, AppBadge.Count(2)))
        val focused = withFocus(rows, Focus(chat.key, until = 1_000))
        assertEquals(
            listOf(HomeRow.App(mail, AppBadge.None), HomeRow.App(chat, AppBadge.Count(2)), FocusRow(chat.key, 1_000)),
            focused,
        )
        assertEquals(chat.key, focusedApp(focused))
    }

    @Test
    fun `a focus on an app not on home changes nothing`() {
        val rows = listOf(HomeRow.App(mail, AppBadge.Dot))
        assertEquals(rows, withFocus(rows, Focus(maps.key, until = 1_000)))
    }

    @Test
    fun `the digest lists the apps that posted most first, without muted ones`() {
        val digest = digestRow(
            away = mapOf(owner(mail) to 1, owner(chat) to 4, owner(maps) to 2),
            catalog = listOf(mail, chat, maps),
            muted = setOf(maps.key),
            previews = emptyMap(),
        )
        assertEquals(listOf(chat, mail), digest?.apps)
        assertEquals(5, digest?.count)
    }

    @Test
    fun `nothing came in, or only from muted apps, means no digest`() {
        assertNull(digestRow(emptyMap(), listOf(mail), emptySet(), emptyMap()))
        assertNull(digestRow(mapOf(owner(mail) to 3), listOf(mail), setOf(mail.key), emptyMap()))
    }
}
