package com.wander.mono.ui.drawer

/**
 * How a name matched a query: its [tier] (lower is better, see [MatchTier]) and the [ranges] of the
 * name's own characters that matched, for highlighting.
 */
internal data class NameMatch(val tier: Int, val ranges: List<IntRange>)

/** Match tiers, best first. */
internal object MatchTier {
    const val EXACT = 0
    const val PREFIX = 1
    const val WORD_PREFIX = 2
    const val INITIALS = 3
    const val SUBSTRING = 4
    const val FUZZY = 5

    /** Query words shorter than this must match as typed: one typo in three letters is another word. */
    const val FUZZY_MIN_LENGTH = 4
}

private val wordBreak = Regex("[^\\p{L}\\p{N}]+")

/** The words of an already folded query. */
internal fun queryWords(query: String): List<String> = query.split(wordBreak).filter { it.isNotEmpty() }

/**
 * How this name matches [query] (already folded and trimmed), null when it does not. Exact name,
 * name prefix, every query word prefixing a word ("mus you" finds "YouTube Music", "tube" finds it
 * by its camel case), initials ("gm" finds "Google Maps", "ytm" "YouTube Music"), substring, then
 * the same word prefixes with one typo in each word of at least [MatchTier.FUZZY_MIN_LENGTH] letters.
 */
internal fun FoldedText.match(query: String): NameMatch? {
    val start = folded.length - folded.trimStart().length
    val end = folded.trimEnd().length
    return when {
        query.isEmpty() || start >= end -> null
        folded.regionMatches(start, query, 0, query.length) && end - start == query.length ->
            NameMatch(MatchTier.EXACT, listOf(originalRange(start, end)))
        folded.startsWith(query, start) ->
            NameMatch(MatchTier.PREFIX, listOf(originalRange(start, start + query.length)))
        else -> wordPrefixes(query)?.let { NameMatch(MatchTier.WORD_PREFIX, it) }
            ?: initials(query)?.let { NameMatch(MatchTier.INITIALS, it) }
            ?: substring(query)?.let { NameMatch(MatchTier.SUBSTRING, it) }
            ?: typoWords(query)?.let { NameMatch(MatchTier.FUZZY, it) }
    }
}

/** Each query word starting a word of the name, wherever in the name. */
private fun FoldedText.wordPrefixes(query: String): List<IntRange>? = allFound(queryWords(query).map { wordPrefix(it) })

/** Where [part] starts a word of the name, null when it starts none. */
private fun FoldedText.wordPrefix(part: String): IntRange? =
    wordStarts.firstOrNull { folded.startsWith(part, it) }?.let { at -> originalRange(at, at + part.length) }

/** Every range of [found], null when there are none or any part was not found. */
private fun allFound(found: List<IntRange?>): List<IntRange>? =
    found.takeIf { it.isNotEmpty() && null !in it }?.filterNotNull()

/** The query's letters as the first letters of words, in order, beginning with the first word. */
private fun FoldedText.initials(query: String): List<IntRange>? {
    val letters = query.filter { it.isLetterOrDigit() }
    if (letters.length < 2 || wordStarts.isEmpty() || folded[wordStarts.first()] != letters.first()) return null
    val picked = ArrayList<IntRange>(letters.length)
    for (at in wordStarts) {
        if (picked.size == letters.length) break
        if (folded[at] == letters[picked.size]) picked.add(originalRange(at, at + 1))
    }
    return picked.takeIf { it.size == letters.length }
}

private fun FoldedText.substring(query: String): List<IntRange>? {
    val at = folded.indexOf(query)
    return if (at < 0) null else listOf(originalRange(at, at + query.length))
}

/**
 * Each query word starting a word of the name, allowing one typo in words long enough
 * ("spotfy" finds "Spotify", "mpas" finds "Maps"; "mpa" is too short to be read as a typo).
 */
private fun FoldedText.typoWords(query: String): List<IntRange>? {
    val parts = queryWords(query)
    if (parts.none { it.length >= MatchTier.FUZZY_MIN_LENGTH }) return null
    return allFound(parts.map { part -> wordPrefix(part) ?: typoWord(part) })
}

/** A word start within one typo of [part]; none for a part too short to be read as a typo. */
private fun FoldedText.typoWord(part: String): IntRange? =
    if (part.length < MatchTier.FUZZY_MIN_LENGTH) null else wordStarts.firstNotNullOfOrNull { at -> typoAt(at, part) }

/** The stretch from [at] within one edit of [part], trying its own length first, then one more and one less. */
private fun FoldedText.typoAt(at: Int, part: String): IntRange? =
    listOf(part.length, part.length + 1, part.length - 1)
        .firstOrNull { size -> at + size <= folded.length && withinOneEdit(folded.substring(at, at + size), part) }
        ?.let { size -> originalRange(at, at + size) }
