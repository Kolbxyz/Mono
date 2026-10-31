package com.wander.mono.ui.drawer

import java.text.Normalizer

/**
 * Reads a search query as a typed command handed to another app: a note ("note buy milk",
 * "noter acheter du lait") or a call ("call Anna", "appeler Anna", "call 06 12 34 56 78"). English
 * and French keywords, matched whatever their case or accents; what follows the keyword is kept as
 * typed. A keyword alone is no command: there must be something to note or someone to call.
 */
internal object CommandQuery {
    /** Longest first, so "take a note" is tried before a shorter keyword could match. */
    private val notePrefixes = longestFirst(
        "note", "notes", "memo", "take a note", "new note",
        "noter", "nouvelle note", "prendre une note", "prends une note",
    )
    private val callPrefixes = longestFirst(
        "call", "dial", "phone", "ring",
        "appeler", "appelle", "appel", "telephoner", "telephoner a", "telephone a", "telephone",
    )

    /** At least this many digits make what follows "call" a number rather than a name. */
    private const val MIN_DIGITS = 3
    private val numberShape = Regex("""\+?[\d\s().\-/]+""")

    /** The text to note, or null when [input] is not a note. */
    fun note(input: String): String? = afterKeyword(input, notePrefixes)

    /** Who (a name, or a number as typed) to call, or null when [input] is not a call. */
    fun callee(input: String): String? = afterKeyword(input, callPrefixes)

    /** [callee] when it is a phone number rather than a name; null otherwise. */
    fun number(callee: String): String? =
        callee.takeIf { numberShape.matches(it) && it.count(Char::isDigit) >= MIN_DIGITS }

    /**
     * What follows the first of [prefixes] that [input] starts with, compared without case or
     * accents and followed by a space or a colon; null when none does, or nothing follows it.
     */
    private fun afterKeyword(input: String, prefixes: List<String>): String? {
        val text = input.trim()
        val plain = withoutAccents(text).map(Char::lowercaseChar).joinToString("")
        val prefix = prefixes.firstOrNull { plain.startsWith(it) && plain.getOrNull(it.length)?.isSeparator() == true }
            ?: return null
        // Folding keeps one letter for one letter, so the prefix is as long in the typed text.
        return text.substring(prefix.length).trimStart { it.isSeparator() }.trim().ifEmpty { null }
    }

    private fun Char.isSeparator() = isWhitespace() || this == ':'

    private fun longestFirst(vararg prefixes: String) = prefixes.sortedByDescending { it.length }
}

/**
 * [text] with its letters' accents dropped ("réveil" is "reveil"), one letter for one letter: what
 * is left of a decomposed letter is its base, the combining marks removed.
 */
internal fun withoutAccents(text: String): String = buildString(text.length) {
    for (char in text) {
        val decomposed = Normalizer.normalize(char.toString(), Normalizer.Form.NFD)
        append(decomposed.firstOrNull { Character.getType(it) != Character.NON_SPACING_MARK.toInt() } ?: char)
    }
}
