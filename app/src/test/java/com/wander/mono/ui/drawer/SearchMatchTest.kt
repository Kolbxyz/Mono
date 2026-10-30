package com.wander.mono.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchMatchTest {
    private fun match(label: String, query: String) = FoldedText(label).match(AppSearch.normalize(query))

    @Test
    fun `folding lowers case and strips accents, precomposed or not`() {
        assertEquals("ecole", FoldedText("École").folded)
        assertEquals("ecole", FoldedText("École").folded)
        assertEquals("ecole", AppSearch.normalize("  ÉCOLE "))
        assertEquals("zurich", FoldedText("Zürich").folded)
    }

    @Test
    fun `folded characters map back to the original ones`() {
        val decomposed = FoldedText("École")
        assertEquals(0..0, decomposed.originalRange(0, 1))
        assertEquals(2..5, decomposed.originalRange(1, 5))
        assertEquals(IntRange.EMPTY, decomposed.originalRange(2, 2))
    }

    @Test
    fun `words start after spaces and punctuation and at camel case`() {
        assertEquals(listOf(0, 3, 8), FoldedText("YouTube Music").wordStarts)
        assertEquals(listOf(0, 3), FoldedText("PDFViewer").wordStarts)
        assertEquals(listOf(0, 3), FoldedText("Wi-Fi").wordStarts)
        assertEquals(listOf(0), FoldedText("École").wordStarts)
        assertEquals(listOf(0), FoldedText("WHATSAPP").wordStarts)
    }

    @Test
    fun `exact and prefix matches highlight the start`() {
        assertEquals(NameMatch(MatchTier.EXACT, listOf(0..3)), match("Maps", "maps"))
        assertEquals(NameMatch(MatchTier.PREFIX, listOf(0..2)), match("Calendar", "cal"))
        assertEquals(NameMatch(MatchTier.EXACT, listOf(0..4)), match("École", "ecole"))
        assertEquals(NameMatch(MatchTier.PREFIX, listOf(0..3)), match("Café Finder", "cafe"))
    }

    @Test
    fun `word prefixes match camel case words in any order`() {
        assertEquals(NameMatch(MatchTier.WORD_PREFIX, listOf(3..6)), match("YouTube", "tube"))
        assertEquals(NameMatch(MatchTier.WORD_PREFIX, listOf(4..7)), match("FaceBook", "book"))
        assertEquals(NameMatch(MatchTier.WORD_PREFIX, listOf(8..10, 0..2)), match("YouTube Music", "mus you"))
    }

    @Test
    fun `initials match the first letters of words in order`() {
        assertEquals(NameMatch(MatchTier.INITIALS, listOf(0..0, 7..7)), match("Google Maps", "gm"))
        assertEquals(NameMatch(MatchTier.INITIALS, listOf(0..0, 5..5)), match("Play Store", "ps"))
        assertEquals(NameMatch(MatchTier.INITIALS, listOf(0..0, 3..3, 8..8)), match("YouTube Music", "ytm"))
        // Skipping a word is fine; starting past the first word is not.
        assertEquals(MatchTier.INITIALS, match("YouTube Music", "ym")?.tier)
        assertNull(match("Google Maps", "mg"))
    }

    @Test
    fun `substring matches anywhere`() {
        assertEquals(NameMatch(MatchTier.SUBSTRING, listOf(3..6)), match("Keynote", "note"))
    }

    @Test
    fun `one typo is forgiven in words of four letters or more`() {
        assertEquals(NameMatch(MatchTier.FUZZY, listOf(0..6)), match("Spotify", "spotfy"))
        assertEquals(NameMatch(MatchTier.FUZZY, listOf(0..3)), match("Maps", "mpas"))
        assertEquals(NameMatch(MatchTier.FUZZY, listOf(0..6)), match("Calendar", "calendr"))
        assertEquals(MatchTier.FUZZY, match("Google Maps", "gogle")?.tier)
        assertEquals(MatchTier.FUZZY, match("Signal", "sginal")?.tier)
    }

    @Test
    fun `typos are not forgiven in short words or twice in one word`() {
        assertNull(match("Maps", "mpa"))
        assertNull(match("Maps", "nap"))
        assertNull(match("Spotify", "sptfy"))
        assertNull(match("Camera", "kamra"))
    }

    @Test
    fun `every word of a query with a typo must still match`() {
        assertEquals(MatchTier.FUZZY, match("Google Maps", "gogle ma")?.tier)
        assertNull(match("Google Maps", "gogle xy"))
    }

    @Test
    fun `nothing matches an empty query or a blank name`() {
        assertNull(FoldedText("Maps").match(""))
        assertNull(match("   ", "a"))
    }
}
