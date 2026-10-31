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
    fun `a clear habit at this hour is the likely favourite, its neighbours helping`() {
        val counts = mapOf(9 to mapOf(mail.key to 3), 8 to mapOf(mail.key to 2, chat.key to 2))
        assertEquals(mail.key, likelyFavorite(counts, 9, listOf(mail.key, chat.key)))
    }

    @Test
    fun `no likely favourite without enough launches or a clear lead`() {
        assertNull(likelyFavorite(mapOf(9 to mapOf(mail.key to 2)), 9, listOf(mail.key)))
        assertNull(likelyFavorite(mapOf(9 to mapOf(mail.key to 4, chat.key to 4)), 9, listOf(mail.key, chat.key)))
    }

    @Test
    fun `midnight's neighbour is the hour before it`() {
        val counts = mapOf(23 to mapOf(maps.key to 6))
        assertEquals(maps.key, likelyFavorite(counts, 0, listOf(maps.key)))
    }

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
