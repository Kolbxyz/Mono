package com.wander.mono.ui.drawer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * The results on screen: those built for the query typed when they are in, else the last ones that
 * were, so a keystroke never flashes "no results" or another query's hits. [settled] says they
 * answer the query typed now; only then may an empty list say there is nothing.
 */
internal data class ShownResults(val results: List<SearchHit> = emptyList(), val settled: Boolean = false) {
    /**
     * What to show once [latest], built for [builtFor], arrives while [query] is typed. A cleared
     * query keeps what was shown, for the results to fade out as they were.
     */
    fun next(query: String, builtFor: String?, latest: List<SearchHit>): ShownResults = when {
        query.isBlank() -> this
        builtFor == query -> ShownResults(latest, settled = true)
        else -> copy(settled = false)
    }
}

/** [ShownResults] for [query], from the drawer's latest [state]. */
@Composable
internal fun rememberShownResults(query: String, state: DrawerUiState): ShownResults {
    // Not snapshot state: it only follows what this composition already reads, and is read right here.
    val last = remember { LastShown() }
    last.value = last.value.next(query, state.resultsQuery, state.results)
    return last.value
}

private class LastShown(var value: ShownResults = ShownResults())
