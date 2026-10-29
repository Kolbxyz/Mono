package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.IconMode

/**
 * The visible (non-hidden) apps with their rail sections, the search results for the current query,
 * and how rows show icons.
 */
data class DrawerUiState(
    val apps: List<DrawerEntry> = emptyList(),
    val sections: List<Section> = emptyList(),
    val results: List<SearchHit> = emptyList(),
    val iconMode: IconMode = IconMode.Default,
)
