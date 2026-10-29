package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.contacts.Contact
import com.wander.mono.core.siblings.WandaTrack
import com.wander.mono.core.system.SettingsPage
import com.wander.mono.core.system.WebSearchTarget

/**
 * Everything search shows for a query, in this order: its [actions] (a sum, a conversion, a timer
 * or an alarm, from [SearchActions]), apps (the best one first), shortcuts, contacts, songs in Wanda,
 * settings pages, and only when nothing else matched, a web search handed to the browser.
 */
internal object SearchResults {
    const val MAX_CONTACTS = 5
    const val MAX_TRACKS = 3

    /** [webTarget] is only asked for when nothing else matched, since it resolves an intent. */
    fun assemble(
        query: String,
        actions: List<SearchHit>,
        entries: List<DrawerEntry>,
        shortcuts: List<AppShortcut>,
        contacts: List<Contact>,
        tracks: List<WandaTrack>,
        pages: List<SettingsPage>,
        webTarget: () -> WebSearchTarget?,
    ): List<SearchHit> {
        if (query.isBlank()) return emptyList()
        val hits = buildList {
            addAll(actions)
            addAll(AppSearch.search(query, entries, shortcuts))
            addAll(AppSearch.rank(query, contacts, label = { it.name }).take(MAX_CONTACTS).map { SearchHit.Person(it) })
            // Wanda already matched and ordered them (most played first); its order is kept.
            addAll(tracks.take(MAX_TRACKS).map { SearchHit.Track(it) })
            val matchingPages = AppSearch.rank(query, pages, label = { it.label }, aliases = { it.keywords })
            addAll(matchingPages.map { SearchHit.Setting(it) })
        }
        return hits.ifEmpty { listOfNotNull(webTarget()?.let { SearchHit.Web(query.trim(), it.appLabel) }) }
    }
}
