package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppShortcut
import java.text.Normalizer
import java.util.Locale

/**
 * Ranked, case- and accent-insensitive matching of names. Tiers, best first: exact name, name
 * prefix, every query word prefixing a word of the name, initials ("ym" finds "YouTube Music"),
 * then plain substring. Within a tier shorter names come first. Apps, shortcuts and settings pages
 * are each ranked this way; [SearchResults] puts the groups in order.
 */
internal object AppSearch {
    private const val EXACT = 0
    private const val PREFIX = 1
    private const val WORD_PREFIX = 2
    private const val INITIALS = 3
    private const val SUBSTRING = 4
    private const val MAX_RESULTS = 50

    private val combiningMarks = Regex("\\p{Mn}+")
    private val wordBreak = Regex("[^\\p{L}\\p{N}]+")

    fun normalize(text: String): String =
        combiningMarks.replace(Normalizer.normalize(text, Normalizer.Form.NFD), "").lowercase(Locale.ROOT).trim()

    /** Lower is better, null when [label] does not match. [query] must already be [normalize]d. */
    fun score(label: String, query: String): Int? {
        if (query.isEmpty()) return null
        val name = normalize(label)
        val words = name.words()
        val queryWords = query.words()
        return when {
            name == query -> EXACT
            name.startsWith(query) -> PREFIX
            queryWords.isNotEmpty() && queryWords.all { part -> words.any { it.startsWith(part) } } -> WORD_PREFIX
            query.length > 1 && words.joinToString("") { it.take(1) }.startsWith(query) -> INITIALS
            name.contains(query) -> SUBSTRING
            else -> null
        }
    }

    /**
     * The [items] matching [query], best first. An item matches by its [label] or, ranked the
     * same way, any of its [aliases]; ties go to the shorter label.
     */
    fun <T> rank(
        query: String,
        items: List<T>,
        label: (T) -> String,
        aliases: (T) -> List<String> = { emptyList() },
    ): List<T> {
        val normalized = normalize(query)
        if (normalized.isEmpty()) return emptyList()
        return items
            .mapNotNull { item ->
                (aliases(item) + label(item)).mapNotNull { score(it, normalized) }.minOrNull()?.let { item to it }
            }
            .sortedWith(
                compareBy<Pair<T, Int>> { it.second }
                    .thenBy { label(it.first).length }
                    .thenBy { label(it.first) },
            )
            .map { it.first }
    }

    /** Every app in [entries] (hidden ones too) matching [query], then every matching shortcut. */
    fun search(query: String, entries: List<DrawerEntry>, shortcuts: List<AppShortcut>): List<SearchHit> {
        val owners = entries.associateBy { it.app.key.packageName to it.app.key.userSerial }
        val apps = rank(query, entries, label = { it.app.label }).map { SearchHit.App(it) }
        val linked = rank(query, shortcuts, label = { it.label })
            .map { SearchHit.Shortcut(it, owners[it.packageName to it.userSerial]?.app) }
        return (apps + linked).take(MAX_RESULTS)
    }

    private fun String.words(): List<String> = split(wordBreak).filter { it.isNotEmpty() }
}
