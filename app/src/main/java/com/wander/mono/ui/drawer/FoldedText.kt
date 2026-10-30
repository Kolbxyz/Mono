package com.wander.mono.ui.drawer

import java.text.Normalizer
import java.util.Locale

/**
 * [original] folded for matching: lower case, accents stripped (each character decomposed, its
 * combining marks dropped), every folded character remembering the original one it came from, so a
 * match is highlighted where it was found. [wordStarts] are where words begin in [folded]: after a
 * space or punctuation, at a lower-to-upper change ("YouTube"), and where a run of capitals hands
 * over to a word ("PDFViewer").
 */
internal class FoldedText(val original: String) {
    val folded: String
    val wordStarts: List<Int>

    /** For each folded character, the original characters it came from: [originStart] until [originEnd]. */
    private val originStart: IntArray
    private val originEnd: IntArray

    init {
        val out = StringBuilder(original.length)
        val starts = ArrayList<Int>()
        val ends = ArrayList<Int>()
        val words = ArrayList<Int>()
        var previous: Int? = null
        var index = 0
        while (index < original.length) {
            val point = original.codePointAt(index)
            val next = index + Character.charCount(point)
            val piece = fold(original.substring(index, next))
            if (piece.isNotEmpty() && isWordStart(previous, point, original.nextLetter(next))) words.add(out.length)
            piece.forEach { char ->
                out.append(char)
                starts.add(index)
                ends.add(next)
            }
            // A combining mark belongs to the letter before it, not between two words.
            if (!isMark(point)) previous = point
            index = next
        }
        folded = out.toString()
        wordStarts = words
        originStart = starts.toIntArray()
        originEnd = ends.toIntArray()
    }

    /** The original characters behind folded [start] until [end] (exclusive); empty when that is. */
    fun originalRange(start: Int, end: Int): IntRange =
        if (end <= start) IntRange.EMPTY else originStart[start] until originEnd[end - 1]

    private fun String.nextLetter(from: Int): Int? {
        var at = from
        while (at < length) {
            val point = codePointAt(at)
            if (!isMark(point)) return point
            at += Character.charCount(point)
        }
        return null
    }

    companion object {
        private val combiningMarks = Regex("\\p{Mn}+")

        /** Lower case without accents, one code point at a time, as both names and queries are compared. */
        fun fold(text: String): String = buildString(text.length) {
            var index = 0
            while (index < text.length) {
                val next = index + Character.charCount(text.codePointAt(index))
                val lower = text.substring(index, next).lowercase(Locale.ROOT)
                append(combiningMarks.replace(Normalizer.normalize(lower, Normalizer.Form.NFD), ""))
                index = next
            }
        }

        private fun isMark(point: Int): Boolean = Character.getType(point) == Character.NON_SPACING_MARK.toInt()

        private fun isWordStart(previous: Int?, point: Int, next: Int?): Boolean {
            val upper = Character.isUpperCase(point)
            return when {
                !Character.isLetterOrDigit(point) -> false
                previous == null || !Character.isLetterOrDigit(previous) -> true
                upper && Character.isLowerCase(previous) -> true
                upper && Character.isUpperCase(previous) -> next != null && Character.isLowerCase(next)
                else -> false
            }
        }
    }
}
