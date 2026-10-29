package com.wander.mono.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Test

class SectionsTest {
    @Test
    fun `one section per letter at its first index, accents folded, others under hash`() {
        val labels = listOf("1Password", "Agenda", "Atlas", "Écran", "Email", "Zoom")
        assertEquals(
            listOf(Section("#", 0), Section("A", 1), Section("E", 3), Section("Z", 5)),
            sectionsOf(labels),
        )
    }
}
