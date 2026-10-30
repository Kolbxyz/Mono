package com.wander.mono.ui.home.folders

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.widgets.PlacedWidget
import com.wander.mono.core.widgets.WidgetPlacement
import com.wander.mono.data.HomeFolder
import com.wander.mono.ui.components.AppBadge
import com.wander.mono.ui.home.HomeRow
import com.wander.mono.ui.home.WidgetHomeRow
import com.wander.mono.ui.home.liftedFirst
import org.junit.Assert.assertEquals
import org.junit.Test

class FolderRowsTest {
    private val mail = app("org.mail")
    private val chat = app("org.chat", AppBadge.Count(2))
    private val maps = app("org.maps", AppBadge.Count(3))
    private val clock = app("com.clock")
    private val talk = HomeFolder("talk", "Talk", listOf(maps.app.key, chat.app.key))

    private fun app(pkg: String, badge: AppBadge = AppBadge.None) =
        HomeRow.App(LaunchableApp(AppKey(pkg, "$pkg.Main", 0), pkg, true), badge)

    private fun widget(id: Int) =
        WidgetHomeRow(PlacedWidget(WidgetPlacement(id, "p/.W", 0, null, 112), "W$id", canReconfigure = false))

    private fun keys(rows: List<HomeRow>) = rows.map { it.key }

    @Test
    fun `no folders leave home as it is`() {
        val rows = listOf(mail, chat)
        assertEquals(rows, withFolders(rows, emptyList()))
    }

    @Test
    fun `a folder takes its first favourite's place and holds its apps in its own order`() {
        val rows = withFolders(listOf(mail, chat, clock, maps), listOf(talk))
        assertEquals(listOf(mail.key, "folder:talk", clock.key), keys(rows))
        assertEquals(listOf(maps, chat), (rows[1] as HomeRow.Folder).apps)
    }

    @Test
    fun `live rows of the folder's apps follow the folder`() {
        val chatWidget = widget(1)
        val clockWidget = widget(2)
        val rows = withFolders(listOf(chat, chatWidget, clock, clockWidget), listOf(talk))
        assertEquals(listOf("folder:talk", chatWidget.key, clock.key, clockWidget.key), keys(rows))
    }

    @Test
    fun `a folder with none of its apps shown is left out`() {
        val rows = listOf(mail, clock)
        assertEquals(rows, withFolders(rows, listOf(talk)))
    }

    @Test
    fun `counts add up and a dot is enough`() {
        assertEquals(AppBadge.Count(5), folderBadge(listOf(chat, maps, mail)))
        assertEquals(AppBadge.Dot, folderBadge(listOf(mail, app("org.x", AppBadge.Dot))))
        assertEquals(AppBadge.None, folderBadge(listOf(mail, clock)))
    }

    @Test
    fun `an open folder shows its apps under it, and edit mode shows them alone`() {
        val grouped = withFolders(listOf(chat, clock, maps), listOf(talk))
        assertEquals(keys(listOf(grouped[0], maps, chat, clock)), keys(openFolders(grouped, setOf("talk"))))
        assertEquals(grouped, openFolders(grouped, emptySet()))
        assertEquals(setOf(maps.key, chat.key), folderMemberKeys(grouped, setOf("talk")))
        assertEquals(keys(listOf(maps, chat, clock)), keys(flattenFolders(grouped)))
    }

    @Test
    fun `a folder goes first when any of its apps does`() {
        val grouped = withFolders(listOf(clock, chat, mail, maps), listOf(talk))
        val lifted = liftedFirst(grouped, setOf(chat.app.key))
        assertEquals(listOf("folder:talk", clock.key, mail.key), keys(lifted))
    }
}
