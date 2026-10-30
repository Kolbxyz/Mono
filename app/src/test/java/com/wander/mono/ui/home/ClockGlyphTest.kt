package com.wander.mono.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class ClockGlyphTest {
    private val hour = "hour"
    private val minute = "minute"
    private val amPm = "amPm"

    @Test
    fun `glyphs are slotted by field, counted from the field's end`() {
        // "9:59 PM"
        val slots = glyphSlots(listOf(hour, null, minute, minute, null, amPm, amPm))
        assertEquals(listOf("hour:0", "literal0:0", "minute:1", "minute:0", "literal1:0", "amPm:1", "amPm:0"), slots)
    }

    @Test
    fun `a new hour digit leaves every other slot where it was`() {
        val before = glyphSlots(listOf(hour, null, minute, minute, null, amPm, amPm))
        val after = glyphSlots(listOf(hour, hour, null, minute, minute, null, amPm, amPm))
        assertEquals(listOf("hour:1") + before, after)
    }

    @Test
    fun `a marker before the time keeps its slots too`() {
        // "PM 9:59" to "PM 10:00", as some locales write it.
        val before = glyphSlots(listOf(amPm, amPm, null, hour, null, minute, minute))
        val after = glyphSlots(listOf(amPm, amPm, null, hour, hour, null, minute, minute))
        assertEquals(before.toSet() + "hour:1", after.toSet())
    }

    @Test
    fun `no glyphs, no slots`() {
        assertEquals(emptyList<String>(), glyphSlots(emptyList()))
    }
}
