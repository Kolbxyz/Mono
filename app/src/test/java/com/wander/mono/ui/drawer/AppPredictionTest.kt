package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.components.AppBadge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppPredictionTest {
    private val maps = AppKey("maps", "maps.Main", 0)
    private val news = AppKey("news", "news.Main", 0)
    private val all = listOf(maps, news)

    @Test
    fun `too few launches suggest nothing`() {
        assertNull(AppPrediction.mostLikely(emptyMap(), 8, all))
        assertNull(AppPrediction.mostLikely(mapOf(8 to mapOf(maps to 4)), 8, all))
    }

    @Test
    fun `the hour counts double next to the hours either side`() {
        val counts = mapOf(7 to mapOf(news to 6), 8 to mapOf(maps to 4, news to 0), 9 to mapOf(maps to 1))
        // Maps: 4 + 1 launches around 8, scored 9; News: 6 launches around 8, scored 6.
        assertEquals(maps, AppPrediction.mostLikely(counts, 8, all))
        assertEquals(news, AppPrediction.mostLikely(counts, 7, all))
    }

    @Test
    fun `midnight's neighbours wrap around the day`() {
        assertEquals(maps, AppPrediction.mostLikely(mapOf(23 to mapOf(maps to 5)), 0, all))
        assertEquals(maps, AppPrediction.mostLikely(mapOf(0 to mapOf(maps to 5)), 23, all))
    }

    @Test
    fun `only apps still listed are suggested`() {
        val counts = mapOf(8 to mapOf(maps to 9, news to 5))
        assertEquals(news, AppPrediction.mostLikely(counts, 8, listOf(news)))
        val listed = DrawerEntry(LaunchableApp(news, "News", isMainProfile = true), AppBadge.None)
        assertEquals(listed, AppPrediction.entryFor(listOf(listed), counts, 8))
        assertNull(AppPrediction.entryFor(emptyList(), counts, 8))
    }

    @Test
    fun `ties always go the same way`() {
        val counts = mapOf(8 to mapOf(maps to 5, news to 5))
        assertEquals(maps, AppPrediction.mostLikely(counts, 8, listOf(news, maps)))
        assertEquals(maps, AppPrediction.mostLikely(counts, 8, listOf(maps, news)))
    }
}
