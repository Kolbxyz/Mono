package com.wander.mono.core.apps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppFilterTest {
    @Test
    fun `component info names the package and class`() {
        assertEquals(
            PackComponent("org.example.mail", "org.example.mail.Inbox"),
            parseComponentInfo("ComponentInfo{org.example.mail/org.example.mail.Inbox}"),
        )
    }

    @Test
    fun `a class starting with a dot is relative to the package`() {
        assertEquals(
            PackComponent("org.example.mail", "org.example.mail.Inbox"),
            parseComponentInfo(" ComponentInfo{org.example.mail/.Inbox} "),
        )
    }

    @Test
    fun `placeholders and broken entries are skipped`() {
        assertNull(parseComponentInfo(":CALENDAR"))
        assertNull(parseComponentInfo("ComponentInfo{org.example.mail}"))
        assertNull(parseComponentInfo("ComponentInfo{/Inbox}"))
        assertNull(parseComponentInfo("ComponentInfo{org.example.mail/}"))
        assertNull(parseComponentInfo("ComponentInfo{org.example.mail/.Inbox"))
        assertNull(parseComponentInfo(null))
    }

    @Test
    fun `the first mapping of an activity wins and empty drawables are ignored`() {
        val map = appFilterMap(
            sequenceOf(
                "ComponentInfo{a.b/a.b.Main}" to "ab_first",
                "ComponentInfo{a.b/a.b.Main}" to "ab_second",
                "ComponentInfo{c.d/.Main}" to " ",
                "ComponentInfo{c.d/.Main}" to "cd",
                ":BROWSER" to "browser",
                "ComponentInfo{e.f/e.f.Main}" to null,
            ),
        )
        assertEquals(
            mapOf(PackComponent("a.b", "a.b.Main") to "ab_first", PackComponent("c.d", "c.d.Main") to "cd"),
            map,
        )
    }
}
