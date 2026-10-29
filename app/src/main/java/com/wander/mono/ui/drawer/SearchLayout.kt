package com.wander.mono.ui.drawer

import androidx.annotation.StringRes
import com.wander.mono.R

/** The kinds of result search groups under their own label, in the order search ranks them. */
internal enum class ResultKind(@StringRes val label: Int) {
    Shortcuts(R.string.search_section_shortcuts),
    Contacts(R.string.search_section_contacts),
    Music(R.string.search_section_music),
    Settings(R.string.search_section_settings),
    Web(R.string.search_section_web),
    Other(R.string.search_section_other),
}

internal data class ResultGroup(val kind: ResultKind, val hits: List<SearchHit>)

/**
 * How the ranked results are laid out, without reordering them: the [top] hit on its own (what the
 * keyboard's Go key opens), the other matching [apps] as one row of icons, then a [groups] per kind
 * in the order its first hit was ranked, each keeping its hits' order.
 */
internal data class SearchLayout(val top: SearchHit?, val apps: List<SearchHit.App>, val groups: List<ResultGroup>) {
    companion object {
        fun of(results: List<SearchHit>): SearchLayout {
            val rest = results.drop(1)
            val groups = rest.filterNot { it is SearchHit.App }
                .groupBy(::kindOf)
                .map { (kind, hits) -> ResultGroup(kind, hits) }
            return SearchLayout(results.firstOrNull(), rest.filterIsInstance<SearchHit.App>(), groups)
        }

        private fun kindOf(hit: SearchHit): ResultKind = when (hit) {
            is SearchHit.Shortcut -> ResultKind.Shortcuts
            is SearchHit.Person -> ResultKind.Contacts
            is SearchHit.Track -> ResultKind.Music
            is SearchHit.Setting -> ResultKind.Settings
            is SearchHit.Web -> ResultKind.Web
            // Apps are never grouped; a sum (or any other quick answer) is normally the top hit.
            else -> ResultKind.Other
        }
    }
}
