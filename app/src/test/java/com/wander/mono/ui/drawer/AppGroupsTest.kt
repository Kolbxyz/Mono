package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.components.AppBadge
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class AppGroupsTest {
    private val day = TimeUnit.DAYS.toMillis(1)
    private val now = 400 * day

    private fun entry(label: String, installedAt: Long? = null, fresh: Boolean = false) = DrawerEntry(
        LaunchableApp(AppKey("p.$label", "p.$label.Main", 0), label, isMainProfile = true, installedAt = installedAt),
        AppBadge.None,
        fresh,
    )

    @Test
    fun `letters group the apps, a New group leads when some are fresh`() {
        val atlas = entry("Atlas")
        val agenda = entry("Agenda", fresh = true)
        val zoom = entry("Zoom")
        val apps = listOf(agenda, atlas, zoom)
        val shape = alphabetical(apps, withNew = true)
        assertEquals(
            listOf(
                AppGroup(GroupHead.New, listOf(agenda)),
                AppGroup(GroupHead.Letter("A"), listOf(agenda, atlas)),
                AppGroup(GroupHead.Letter("Z"), listOf(zoom)),
            ),
            shape.groups,
        )
        assertEquals(listOf(Section("A", 0), Section("Z", 2)), shape.sections)
        assertEquals(1, shape.lead)
    }

    @Test
    fun `no New group without fresh apps or with new apps off`() {
        val apps = listOf(entry("Atlas", fresh = true))
        assertEquals(0, alphabetical(apps, withNew = false).lead)
        assertEquals(0, alphabetical(listOf(entry("Atlas")), withNew = true).lead)
    }

    @Test
    fun `newest first under how long ago, unknown install times last in name order`() {
        val today = entry("Today", now - day / 2)
        val week = entry("Week", now - 3 * day)
        val month = entry("Month", now - 20 * day)
        val old = entry("Old", now - 90 * day)
        val workA = entry("A work")
        val workB = entry("B work")
        val shape = byInstallAge(listOf(workA, old, week, workB, today, month), now)
        assertEquals(
            listOf(
                AppGroup(GroupHead.Age(InstallAge.Day), listOf(today)),
                AppGroup(GroupHead.Age(InstallAge.Week), listOf(week)),
                AppGroup(GroupHead.Age(InstallAge.Month), listOf(month)),
                AppGroup(GroupHead.Age(InstallAge.Earlier), listOf(old, workA, workB)),
            ),
            shape.groups,
        )
        assertEquals(emptyList<Section>(), shape.sections)
    }

    @Test
    fun `group keys never clash with each other or the profile cards`() {
        val keys = listOf(
            AppGroup(GroupHead.New, emptyList()),
            AppGroup(GroupHead.Letter("N"), emptyList()),
            AppGroup(GroupHead.Age(InstallAge.Day), emptyList()),
        ).map { it.key }
        assertEquals(5, (keys + "private" + "work:10").toSet().size)
    }
}
