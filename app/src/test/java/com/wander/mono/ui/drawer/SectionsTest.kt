package com.wander.mono.ui.drawer

import java.text.Collator
import java.text.RuleBasedCollator
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class SectionsTest {
    private val english = primaryCollator(Locale.ENGLISH)

    @Test
    fun `one section per letter at its first index, accents folded, others under hash`() {
        val labels = listOf("1Password", "Agenda", "Atlas", "Écran", "Email", "Zoom")
        assertEquals(
            listOf(Section("#", 0), Section("A", 1), Section("E", 3), Section("Z", 5)),
            sectionsOf(labels, Locale.ENGLISH),
        )
    }

    @Test
    fun `a letter the collator sorts as a variant goes under its base letter`() {
        // Android's collator treats Ø and Ł as O and L with a mark; the JVM's needs telling.
        val icuLike = RuleBasedCollator(
            (Collator.getInstance(Locale.ENGLISH) as RuleBasedCollator).rules + "& o ; ø & O ; Ø & l ; ł & L ; Ł",
        ).apply { strength = Collator.PRIMARY }
        assertEquals("O", sectionLetter("Ørsted", icuLike))
        assertEquals("L", sectionLetter("Łódź Maps", icuLike))
    }

    @Test
    fun `each letter's apps stay in one run in the catalog's own order`() {
        val sorter = Collator.getInstance(Locale.ENGLISH)
        val labels = listOf("Otto", "Lyft", "Ørsted", "Æon", "Lens", "Łódź Maps", "Oasis", "Atlas", "Zoom")
            .sortedWith(sorter)
        val letters = labels.map { sectionLetter(it, english) }
        val runs = letters.zipWithNext().count { (a, b) -> a != b } + 1
        assertEquals(letters.toString(), letters.distinct().size, runs)
    }

    @Test
    fun `other scripts keep their own letter and a blank name goes under hash`() {
        assertEquals("Б", sectionLetter("Банк", english))
        assertEquals("#", sectionLetter("  ", english))
    }
}
