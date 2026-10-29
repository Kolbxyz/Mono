package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Test

class DrawerListsTest {
    private val camera = entry("org.camera", "Camera", serial = 0)
    private val bank = entry("com.bank", "Bank", serial = 11, private = true)
    private val notes = entry("org.notes", "Notes", serial = 11, private = true)
    private val workMail = entry("com.mail", "Mail", serial = 10)

    private fun entry(pkg: String, label: String, serial: Long, private: Boolean = false) = DrawerEntry(
        LaunchableApp(AppKey(pkg, "$pkg.Main", serial), label, isMainProfile = serial == 0L, isPrivate = private),
        AppBadge.None,
    )

    @Test
    fun `private apps leave the A to Z list for their own section, in order`() {
        val lists = drawerLists(listOf(bank, camera, workMail, notes), hidden = emptySet())
        assertEquals(listOf(camera, workMail), lists.apps)
        assertEquals(listOf(bank, notes), lists.privateApps)
    }

    @Test
    fun `hidden apps are in neither list`() {
        val lists = drawerLists(listOf(bank, camera, notes), hidden = setOf(camera.app.key, notes.app.key))
        assertEquals(emptyList<DrawerEntry>(), lists.apps)
        assertEquals(listOf(bank), lists.privateApps)
    }

    @Test
    fun `picking for home offers no private app but keeps every other result`() {
        val calculation = SearchHit.Calculation("4")
        val results = listOf(calculation, SearchHit.App(bank), SearchHit.App(camera), SearchHit.App(workMail))
        assertEquals(listOf(calculation, SearchHit.App(camera), SearchHit.App(workMail)), pickable(results))
    }
}
