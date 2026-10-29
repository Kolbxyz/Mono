package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppShortcut
import java.text.Normalizer
import java.util.Locale

/**
 * Ranked, case- and accent-insensitive search over app names and shortcut labels. Tiers, best
 * first: exact name, name prefix, every query word prefixing a word of the name, initials ("ym"
 * finds "YouTube Music"), then plain substring. Within a tier apps come before shortcuts, then
 * shorter names first.
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

    /** Every app in [entries] (hidden ones too) and every shortcut matching [query], best first. */
    fun search(query: String, entries: List<DrawerEntry>, shortcuts: List<AppShortcut>): List<SearchHit> {
        val normalized = normalize(query)
        if (normalized.isEmpty()) return emptyList()
        val owners = entries.associateBy { it.app.key.packageName to it.app.key.userSerial }
        val candidates = entries.map { SearchHit.App(it) } +
            shortcuts.map { SearchHit.Shortcut(it, owners[it.packageName to it.userSerial]?.app) }
        return candidates
            .mapNotNull { hit -> score(hit.label, normalized)?.let { hit to it } }
            .sortedWith(
                compareBy<Pair<SearchHit, Int>> { it.second }
                    .thenBy { if (it.first is SearchHit.App) 0 else 1 }
                    .thenBy { it.first.label.length }
                    .thenBy { it.first.label }
            )
            .take(MAX_RESULTS)
            .map { it.first }
    }

    private fun String.words(): List<String> = split(wordBreak).filter { it.isNotEmpty() }
}
