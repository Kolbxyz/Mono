package com.wander.mono.ui.sheet

import org.junit.Assert.assertEquals
import org.junit.Test

class SheetUsageTest {
    @Test
    fun `a readable app reserves the line while its time is read`() {
        assertEquals(SheetUsage.Reading, usageBeforeReading(show = true, readable = true))
    }

    @Test
    fun `off in settings or unreadable leaves no line at all`() {
        assertEquals(SheetUsage.Hidden, usageBeforeReading(show = false, readable = true))
        assertEquals(SheetUsage.Hidden, usageBeforeReading(show = true, readable = false))
    }

    @Test
    fun `a finished read shows the time, or nothing when access was gone`() {
        assertEquals(SheetUsage.Today(42_000L), usageRead(42_000L))
        assertEquals(SheetUsage.Hidden, usageRead(null))
    }
}
