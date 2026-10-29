package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.apps.WorkProfile
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Test

class DrawerListsTest {
    private val camera = entry("org.camera", "Camera", serial = 0)
    private val bank = entry("com.bank", "Bank", serial = 11, private = true)
    private val notes = entry("org.notes", "Notes", serial = 11, private = true)
    private val workMail = entry("com.mail", "Mail", serial = 10)
    private val work = WorkProfile(serial = 10, paused = false, canToggle = true)
    private val pausedWork = work.copy(paused = true)

    private fun entry(pkg: String, label: String, serial: Long, private: Boolean = false) = DrawerEntry(
        LaunchableApp(AppKey(pkg, "$pkg.Main", serial), label, isMainProfile = serial == 0L, isPrivate = private),
        AppBadge.None,
    )

    @Test
    fun `private and work apps leave the A to Z list for their own sections, in order`() {
        val lists = drawerLists(listOf(bank, camera, workMail, notes), hidden = emptySet(), listOf(work))
        assertEquals(listOf(camera), lists.apps)
        assertEquals(listOf(bank, notes), lists.privateApps)
        assertEquals(listOf(WorkSection(work, listOf(workMail))), lists.work)
    }

    @Test
    fun `hidden apps are in no list`() {
        val hidden = setOf(camera.app.key, notes.app.key)
        val lists = drawerLists(listOf(bank, camera, notes), hidden, work = emptyList())
        assertEquals(emptyList<DrawerEntry>(), lists.apps)
        assertEquals(listOf(bank), lists.privateApps)
    }

    @Test
    fun `a paused work profile lists no apps and stays out of search`() {
        val all = listOf(camera, workMail)
        val lists = drawerLists(all, emptySet(), listOf(pausedWork))
        assertEquals(listOf(WorkSection(pausedWork, emptyList())), lists.work)
        assertEquals(listOf(camera), searchableApps(all, listOf(pausedWork)))
        assertEquals(all, searchableApps(all, listOf(work)))
        val shortcuts = listOf(
            AppShortcut("com.mail", "inbox", 10, "Inbox"),
            AppShortcut("org.camera", "selfie", 0, "Selfie"),
        )
        assertEquals(listOf(shortcuts[1]), searchableShortcuts(shortcuts, listOf(pausedWork)))
    }

    @Test
    fun `apps of a work profile not known yet wait out of the A to Z list`() {
        val lists = drawerLists(listOf(camera, workMail), hidden = emptySet(), work = emptyList())
        assertEquals(listOf(camera), lists.apps)
        assertEquals(emptyList<WorkSection>(), lists.work)
    }

    @Test
    fun `picking for home offers no private app but keeps every other result`() {
        val calculation = SearchHit.Calculation("4")
        val results = listOf(calculation, SearchHit.App(bank), SearchHit.App(camera), SearchHit.App(workMail))
        assertEquals(listOf(calculation, SearchHit.App(camera), SearchHit.App(workMail)), pickable(results))
    }
}
