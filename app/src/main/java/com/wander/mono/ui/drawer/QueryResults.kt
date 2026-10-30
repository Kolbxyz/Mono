package com.wander.mono.ui.drawer

/**
 * What a search source that answers off the main path (contacts, Wanda) found for [query], trimmed
 * as it was asked; tagged so a newer query's results never mix with an older query's.
 */
data class QueryResults<T>(val query: String, val items: List<T>) {
    /** Whether these are the answer for [typed], as typed in the search field. */
    fun isFor(typed: String): Boolean = query == typed.trim()

    /** [items] when they answer [typed]; nothing while the source is still on an older query. */
    fun itemsFor(typed: String): List<T> = if (isFor(typed)) items else emptyList()
}

/** [typed] once every one of [sources] has answered it; null while any is still on an older query. */
internal fun builtFor(typed: String, vararg sources: QueryResults<*>): String? =
    typed.takeIf { sources.all { it.isFor(typed) } }
