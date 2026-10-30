package com.wander.mono.data

import java.util.Locale

/** A search that opened something: what was typed, and the key of the result it opened. */
data class RecentSearch(val query: String, val hitKey: String)

/** The list rules of [SearchHistory] and how it is stored: one `key<TAB>query` line per search. */
internal object RecentSearches {
    const val MAX = 8
    private const val FIELD = '\t'
    private val spaces = Regex("\\s+")

    /**
     * [list] with [search] first. The query is trimmed and kept on one line; a blank one is not kept.
     * An older search with the same query (whatever its case) or the same result gives way to it, so
     * each chip offers something different.
     */
    fun remembered(list: List<RecentSearch>, search: RecentSearch): List<RecentSearch> {
        val query = search.query.replace(spaces, " ").trim()
        val unstorable = search.hitKey.isEmpty() || search.hitKey.any { it == FIELD || it == '\n' }
        if (query.isEmpty() || unstorable) return list
        val entry = RecentSearch(query, search.hitKey)
        val others = list.filterNot { same(it.query, query) || it.hitKey == entry.hitKey }
        return (listOf(entry) + others).take(MAX)
    }

    /** The key of the result last opened for [query], null when it was never searched. */
    fun preferredFor(list: List<RecentSearch>, query: String): String? =
        list.firstOrNull { same(it.query, query.replace(spaces, " ").trim()) }?.hitKey

    fun encode(list: List<RecentSearch>): String = list.joinToString("\n") { "${it.hitKey}$FIELD${it.query}" }

    /** [encode]d searches read back; a line that is not one is skipped. */
    fun decode(stored: String): List<RecentSearch> = stored.lines().mapNotNull { line ->
        val split = line.indexOf(FIELD)
        if (split <= 0 || split == line.length - 1) return@mapNotNull null
        RecentSearch(query = line.substring(split + 1), hitKey = line.substring(0, split))
    }

    private fun same(a: String, b: String) = a.lowercase(Locale.ROOT) == b.lowercase(Locale.ROOT)
}
