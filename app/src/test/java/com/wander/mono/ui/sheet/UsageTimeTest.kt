package com.wander.mono.ui.sheet

import org.junit.Assert.assertEquals
import org.junit.Test

class UsageTimeTest {
    private val minute = 60_000L

    @Test
    fun `under a minute is said so, not zero`() {
        assertEquals(UsageTime.UnderAMinute, usageTime(59_999))
    }

    @Test
    fun `minutes round down`() {
        assertEquals(UsageTime.Minutes(42), usageTime(42 * minute + 59_000))
    }

    @Test
    fun `an hour or more splits into hours and minutes`() {
        assertEquals(UsageTime.Hours(1, 0), usageTime(60 * minute))
        assertEquals(UsageTime.Hours(2, 5), usageTime(125 * minute))
    }
}
