package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppShortcut

/**
 * Ranked, case- and accent-insensitive matching of names, forgiving one typo in longer words. Tiers,
 * best first, are [MatchTier]'s: exact name, name prefix, word prefixes, initials, substring, then
 * word prefixes with a typo. Within a tier shorter names come first. Apps, shortcuts, contacts and
 * settings pages are each ranked this way; [SearchResults] puts the groups in order.
 */
internal object AppSearch {
    private const val MAX_RESULTS = 50

    /** [text] folded as names are compared ([FoldedText.fold]) and trimmed: what [score] takes as a query. */
    fun normalize(text: String): String = FoldedText.fold(text).trim()

    /** Lower is better, null when [label] does not match. [query] must already be [normalize]d. */
    fun score(label: String, query: String): Int? = FoldedText(label).match(query)?.tier

    /**
     * The [items] matching [query], best first, each with the characters of its label that matched.
     * An item matches by its [label] or, ranked the same way, any of its [aliases] (then nothing of the
     * label is highlighted unless it matched too); ties go to the shorter label.
     */
    fun <T> rankMatched(
        query: String,
        items: List<T>,
        label: (T) -> String,
        aliases: (T) -> List<String> = { emptyList() },
    ): List<Ranked<T>> {
        val normalized = normalize(query)
        if (normalized.isEmpty()) return emptyList()
        return items
            .mapNotNull { item ->
                val own = FoldedText(label(item)).match(normalized)
                val alias = aliases(item).mapNotNull { score(it, normalized) }.minOrNull()
                val tier = listOfNotNull(own?.tier, alias).minOrNull() ?: return@mapNotNull null
                Scored(Ranked(item, own?.ranges.orEmpty()), tier, label(item))
            }
            .sortedWith(compareBy<Scored<T>> { it.tier }.thenBy { it.label.length }.thenBy { it.label })
            .map { it.ranked }
    }

    /** [rankMatched] without the matched characters. */
    fun <T> rank(
        query: String,
        items: List<T>,
        label: (T) -> String,
        aliases: (T) -> List<String> = { emptyList() },
    ): List<T> = rankMatched(query, items, label, aliases).map { it.item }

    /** Every app in [entries] (hidden ones too) matching [query], then every matching shortcut. */
    fun search(query: String, entries: List<DrawerEntry>, shortcuts: List<AppShortcut>): List<SearchHit> {
        val owners = entries.associateBy { it.app.key.packageName to it.app.key.userSerial }
        val apps = rankMatched(query, entries, label = { it.app.label }).map { SearchHit.App(it.item, it.matched) }
        val linked = rankMatched(query, shortcuts, label = { it.label }).map { (shortcut, matched) ->
            SearchHit.Shortcut(shortcut, owners[shortcut.packageName to shortcut.userSerial]?.app, matched = matched)
        }
        return (apps + linked).take(MAX_RESULTS)
    }

    private data class Scored<T>(val ranked: Ranked<T>, val tier: Int, val label: String)
}

/** A search match and the characters of its label that matched, for highlighting. */
internal data class Ranked<T>(val item: T, val matched: List<IntRange>)
