package com.wander.mono.core.usage

import org.junit.Assert.assertEquals
import org.junit.Test

class ForegroundTimeTest {
    private fun on(activity: String, time: Long) = ActivityMark(activity, resumed = true, time)

    private fun off(activity: String, time: Long) = ActivityMark(activity, resumed = false, time)

    @Test
    fun `sums each stretch in front`() {
        val marks = listOf(on("A", 10), off("A", 30), on("A", 50), off("A", 55))
        assertEquals(25L, foregroundMillis(marks, start = 0, end = 100))
    }

    @Test
    fun `moving between the app's own activities is one stretch`() {
        val marks = listOf(on("A", 10), on("B", 20), off("A", 21), off("B", 40))
        assertEquals(30L, foregroundMillis(marks, start = 0, end = 100))
    }

    @Test
    fun `an app in front since before midnight counts from the start`() {
        val marks = listOf(off("A", 15), on("A", 50), off("A", 60))
        assertEquals(25L, foregroundMillis(marks, start = 0, end = 100))
    }

    @Test
    fun `an app still in front counts until now`() {
        assertEquals(30L, foregroundMillis(listOf(on("A", 70)), start = 0, end = 100))
    }

    @Test
    fun `no marks is no time`() {
        assertEquals(0L, foregroundMillis(emptyList(), start = 0, end = 100))
    }
}
