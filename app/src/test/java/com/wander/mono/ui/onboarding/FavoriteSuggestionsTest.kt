package com.wander.mono.ui.onboarding

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteSuggestionsTest {
    private fun app(name: String, main: Boolean = true, inPrivate: Boolean = false, activity: String = "Main") =
        LaunchableApp(
            AppKey("pkg.$name", "pkg.$name.$activity", if (main) 0L else 10L),
            name,
            isMainProfile = main,
            isPrivate = inPrivate,
        )

    private val camera = app("Camera")
    private val maps = app("Maps")
    private val notes = app("Notes")
    private val alphabetical = listOf(camera, maps, notes)
    private val minutes = 60_000L

    @Test
    fun `without usage access the list stays alphabetical and nothing is ticked`() {
        val suggestions = favoriteSuggestions(alphabetical, todayMillis = null)
        assertEquals(alphabetical, suggestions.apps)
        assertEquals(emptyList<AppKey>(), suggestions.preselected)
    }

    @Test
    fun `most used first, unused apps keep their alphabetical order`() {
        val usage = mapOf("pkg.Notes" to 30 * minutes, "pkg.Maps" to 5 * minutes)
        val suggestions = favoriteSuggestions(alphabetical, usage)
        assertEquals(listOf(notes, maps, camera), suggestions.apps)
        assertEquals(listOf(notes.key, maps.key), suggestions.preselected)
    }

    @Test
    fun `a glance under a minute is listed but not ticked`() {
        val suggestions = favoriteSuggestions(alphabetical, mapOf("pkg.Maps" to 59_000L))
        assertEquals(listOf(maps, camera, notes), suggestions.apps)
        assertEquals(emptyList<AppKey>(), suggestions.preselected)
    }

    @Test
    fun `at most five apps are ticked`() {
        val apps = ('A'..'H').map { app(it.toString()) }
        val usage = apps.mapIndexed { index, entry -> entry.key.packageName to (index + 1) * minutes }.toMap()
        val suggestions = favoriteSuggestions(apps, usage)
        assertEquals(MAX_PRESELECTED, suggestions.preselected.size)
        assertEquals(apps.takeLast(MAX_PRESELECTED).reversed().map { it.key }, suggestions.preselected)
    }

    @Test
    fun `private space apps are never offered and work apps get no main profile usage`() {
        val secret = app("Secret", main = false, inPrivate = true)
        val workMaps = app("Maps", main = false)
        val suggestions = favoriteSuggestions(listOf(maps, workMaps, secret), mapOf("pkg.Maps" to 10 * minutes))
        assertEquals(listOf(maps, workMaps), suggestions.apps)
        assertEquals(listOf(maps.key), suggestions.preselected)
    }

    @Test
    fun `an app with two launcher entries is ticked once`() {
        val dialer = app("Phone")
        val contacts = app("Phone", activity = "Contacts")
        val suggestions = favoriteSuggestions(listOf(dialer, contacts), mapOf("pkg.Phone" to 10 * minutes))
        assertEquals(listOf(dialer.key), suggestions.preselected)
    }
}
