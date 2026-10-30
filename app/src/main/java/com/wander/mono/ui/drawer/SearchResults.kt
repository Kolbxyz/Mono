package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.contacts.Contact
import com.wander.mono.core.notifications.NotificationPreview
import com.wander.mono.core.siblings.WandaTrack
import com.wander.mono.core.system.SettingsPage
import com.wander.mono.core.system.WebSearchTarget

/**
 * Everything search shows for a query, in this order: its [actions] (a sum, a conversion, a timer
 * or an alarm, from [SearchActions]), apps (the best one first), shortcuts, the query as a link to
 * open, contacts, notifications, songs in Wanda, settings pages, and only when nothing else matched,
 * a web search handed to the browser; then, last, sharing the query when it was pasted text. A
 * result the user opened for this very query before ([SearchHistory][com.wander.mono.data.SearchHistory])
 * comes first.
 */
internal object SearchResults {
    const val MAX_CONTACTS = 5
    const val MAX_TRACKS = 3

    /**
     * [handOffs] are [SearchLinks]'s for the query; [notices] the posted notifications to look in;
     * [preferred] the key of the result to put first. [webTarget] is only asked for when nothing else
     * matched, since it resolves an intent.
     */
    fun assemble(
        query: String,
        actions: List<SearchHit>,
        entries: List<DrawerEntry>,
        shortcuts: List<AppShortcut>,
        contacts: List<Contact>,
        tracks: List<WandaTrack>,
        pages: List<SettingsPage>,
        handOffs: List<SearchHit> = emptyList(),
        notices: List<NotificationPreview> = emptyList(),
        preferred: String? = null,
        webTarget: () -> WebSearchTarget?,
    ): List<SearchHit> {
        if (query.isBlank()) return emptyList()
        val hits = buildList {
            addAll(actions)
            addAll(AppSearch.search(query, entries, shortcuts))
            addAll(handOffs.filterIsInstance<SearchHit.Link>())
            val people = AppSearch.rankMatched(query, contacts, label = { it.name }).take(MAX_CONTACTS)
            addAll(people.map { SearchHit.Person(it.item, it.matched) })
            addAll(SearchNotices.find(query, notices, entries))
            // Wanda already matched and ordered them (most played first); its order is kept.
            addAll(tracks.take(MAX_TRACKS).map { SearchHit.Track(it) })
            val matchingPages = AppSearch.rankMatched(query, pages, label = { it.label }, aliases = { it.keywords })
            addAll(matchingPages.map { SearchHit.Setting(it.item, it.matched) })
        }
        val found = hits.ifEmpty { listOfNotNull(webTarget()?.let { SearchHit.Web(query.trim(), it.appLabel) }) }
        // Sharing is no answer to the query: it never keeps the web search out.
        return preferredFirst(found + handOffs.filterIsInstance<SearchHit.ShareText>(), preferred)
    }

    /** [hits] with the one keyed [preferred] moved to the front, when it is among them. */
    fun preferredFirst(hits: List<SearchHit>, preferred: String?): List<SearchHit> {
        val first = hits.indexOfFirst { it.key == preferred }
        if (first <= 0) return hits
        return listOf(hits[first]) + hits.filterIndexed { index, _ -> index != first }
    }
}
