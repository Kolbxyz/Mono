package com.wander.mono.ui.drawer

/** The visible (non-hidden) apps with their rail sections, and the search results for the current query. */
data class DrawerUiState(
    val apps: List<DrawerEntry> = emptyList(),
    val sections: List<Section> = emptyList(),
    val results: List<SearchHit> = emptyList(),
)
