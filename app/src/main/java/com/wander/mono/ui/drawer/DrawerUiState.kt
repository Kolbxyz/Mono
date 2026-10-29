package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.PrivateSpaceState
import com.wander.mono.core.system.WallpaperTone
import com.wander.mono.data.settings.DrawerBackground
import com.wander.mono.data.settings.IconMode

/**
 * The visible (non-hidden) main-profile apps with their rail sections, the search results for the
 * current query, how rows show icons, whether search leaves out hidden apps until the screen lock is
 * confirmed, the work profiles and the private space at the bottom with their apps (listed only while
 * open), and what the drawer is drawn on.
 */
data class DrawerUiState(
    val apps: List<DrawerEntry> = emptyList(),
    val sections: List<Section> = emptyList(),
    val results: List<SearchHit> = emptyList(),
    val iconMode: IconMode = IconMode.Colour,
    val hiddenLocked: Boolean = false,
    val privateSpace: PrivateSpaceState = PrivateSpaceState.Absent,
    val privateApps: List<DrawerEntry> = emptyList(),
    val work: List<WorkSection> = emptyList(),
    val look: DrawerLook = DrawerLook(),
)

/** What the drawer is drawn on, and what its scrim needs to keep text legible over the wallpaper. */
data class DrawerLook(
    val background: DrawerBackground = DrawerBackground.Scrim,
    val tone: WallpaperTone? = null,
    val highContrast: Boolean = false,
)
