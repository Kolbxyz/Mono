package com.wander.mono.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppBadgeTest {
    @Test
    fun `no unread items means no badge, whatever the style`() {
        assertEquals(AppBadge.None, appBadge(0, showCounts = true))
        assertEquals(AppBadge.None, appBadge(0, showCounts = false))
    }

    @Test
    fun `counts show as a pill, or as a dot when counts are off`() {
        assertEquals(AppBadge.Count(3), appBadge(3, showCounts = true))
        assertEquals(AppBadge.Dot, appBadge(3, showCounts = false))
    }

    @Test
    fun `counts up to 99 show exactly`() {
        val badge = AppBadge.Count(99)
        assertEquals(99, badge.shown)
        assertFalse(badge.isCapped)
    }

    @Test
    fun `counts past 99 cap at 99 plus but keep the exact count`() {
        val badge = AppBadge.Count(250)
        assertEquals(99, badge.shown)
        assertTrue(badge.isCapped)
        assertEquals(250, badge.count)
    }
}
