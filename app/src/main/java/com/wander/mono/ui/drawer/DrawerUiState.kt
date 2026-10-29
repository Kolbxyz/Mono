package com.wander.mono.ui.drawer

import com.wander.mono.data.settings.IconMode

/**
 * The visible (non-hidden) apps with their rail sections, the search results for the current query,
 * how rows show icons, and whether search leaves out hidden apps until the screen lock is confirmed.
 */
data class DrawerUiState(
    val apps: List<DrawerEntry> = emptyList(),
    val sections: List<Section> = emptyList(),
    val results: List<SearchHit> = emptyList(),
    val iconMode: IconMode = IconMode.Colour,
    val hiddenLocked: Boolean = false,
)
