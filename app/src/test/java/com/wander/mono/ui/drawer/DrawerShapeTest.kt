package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppCategory
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.apps.PrivateSpaceState
import com.wander.mono.core.apps.WorkProfile
import com.wander.mono.data.settings.DrawerSort
import com.wander.mono.ui.components.AppBadge
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawerShapeTest {
    private val now = TimeUnit.DAYS.toMillis(100)
    private val camera = entry("Camera", 0, AppCategory.Image, installedAt = now - 1_000)
    private val chess = entry("Chess", 0, AppCategory.Games)
    private val mail = entry("Mail", 10, AppCategory.Productivity)
    private val atlas = entry("Atlas", 10, AppCategory.Maps)
    private val bank = entry("Bank", 11, AppCategory.Other, private = true)
    private val work = WorkProfile(serial = 10, paused = false, canToggle = true)

    private fun entry(
        label: String,
        serial: Long,
        category: AppCategory,
        installedAt: Long? = null,
        private: Boolean = false,
    ) = DrawerEntry(
        LaunchableApp(
            AppKey("p.$label", "p.$label.Main", serial),
            label,
            isMainProfile = serial == 0L,
            isPrivate = private,
            category = category,
            installedAt = installedAt,
        ),
        AppBadge.None,
    )

    private val state = DrawerUiState(
        apps = listOf(camera, chess),
        privateApps = listOf(bank),
        work = listOf(WorkSection(work, listOf(mail, atlas))),
    )

    @Test
    fun `everyone's apps by default, the new one marked and gathered at the top`() {
        val shaped = state.shaped(ListChoices(), emptySet(), now)
        assertTrue(shaped.apps.first { it.app == camera.app }.fresh)
        assertEquals(GroupHead.New, shaped.groups.first().head)
        assertEquals(1, shaped.railLead)
        assertEquals(state.work, shaped.work)
        assertTrue(shaped.controls.hasWork)
    }

    @Test
    fun `an opened new app is no longer marked`() {
        val shaped = state.shaped(ListChoices(), setOf(camera.app.key), now)
        assertFalse(shaped.apps.any { it.fresh })
        assertEquals(0, shaped.railLead)
    }

    @Test
    fun `Work letters the work apps, keeps their cards bare and drops the private space`() {
        val shaped = state.shaped(ListChoices(profile = ProfileFilter.Work), emptySet(), now)
        assertEquals(listOf("Atlas", "Mail"), shaped.apps.map { it.app.label })
        assertEquals(listOf(WorkSection(work, emptyList())), shaped.work)
        assertEquals(emptyList<DrawerEntry>(), shaped.privateApps)
    }

    @Test
    fun `Personal drops the work cards; without a work profile the filter falls back to All`() {
        val personal = state.shaped(ListChoices(profile = ProfileFilter.Personal), emptySet(), now)
        assertEquals(emptyList<WorkSection>(), personal.work)
        val noWork = state.copy(work = emptyList()).shaped(ListChoices(profile = ProfileFilter.Work), emptySet(), now)
        assertEquals(ProfileFilter.All, noWork.controls.choices.profile)
        assertEquals(2, noWork.apps.size)
    }

    @Test
    fun `only present categories are offered, and one narrows the list while the chips show`() {
        val chosen = ListChoices(showCategories = true, category = AppCategory.Games)
        val shaped = state.shaped(chosen, emptySet(), now)
        assertEquals(listOf(AppCategory.Games, AppCategory.Image), shaped.controls.categories)
        assertEquals(listOf("Chess"), shaped.apps.map { it.app.label })
        assertEquals(emptyList<DrawerEntry>(), shaped.work.single().apps)
        // Chips hidden: the category is kept aside, every app shows.
        assertEquals(2, state.shaped(chosen.copy(showCategories = false), emptySet(), now).apps.size)
        // A category none of the listed apps has is dropped.
        val gone = state.shaped(chosen.copy(category = AppCategory.News), emptySet(), now)
        assertEquals(null, gone.controls.choices.category)
        assertEquals(2, gone.apps.size)
    }

    @Test
    fun `newest first drops the rail`() {
        val shaped = state.shaped(ListChoices(sort = DrawerSort.Recent), emptySet(), now)
        assertEquals(emptyList<Section>(), shaped.sections)
        assertEquals(GroupHead.Age(InstallAge.Day), shaped.groups.first().head)
    }

    @Test
    fun `a locked or absent private space lists and finds none of its apps`() {
        val withHits = state.copy(results = listOf(SearchHit.App(bank), SearchHit.App(camera)))
        val locked = withHits.withPrivateSpace(PrivateSpaceState.Present(locked = true, canOpenSettings = false))
        assertEquals(emptyList<DrawerEntry>(), locked.privateApps)
        assertEquals(listOf(SearchHit.App(camera)), locked.results)
        assertEquals(listOf(SearchHit.App(camera)), withHits.withPrivateSpace(PrivateSpaceState.Absent).results)
        val open = withHits.withPrivateSpace(PrivateSpaceState.Present(locked = false, canOpenSettings = false))
        assertEquals(listOf(bank), open.privateApps)
        assertEquals(2, open.results.size)
    }
}
