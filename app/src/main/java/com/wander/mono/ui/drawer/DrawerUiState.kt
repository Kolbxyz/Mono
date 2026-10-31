package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.PrivateSpaceState
import com.wander.mono.core.system.WallpaperTone
import com.wander.mono.data.settings.DrawerBackground
import com.wander.mono.data.settings.IconMode

/**
 * The listed apps (main profile, or work under the Work filter, narrowed by category) with their
 * rail sections and the [groups] the list shows them in, the search results for the current
 * query, how rows show icons, whether search leaves out hidden apps until the screen lock is
 * confirmed, the work profiles and the private space at the bottom with their apps (listed only while
 * open), what the drawer is drawn on, whether opening it brings up the keyboard, which edge the
 * letter rail is on, and the toolbar's [controls].
 */
data class DrawerUiState(
    val apps: List<DrawerEntry> = emptyList(),
    val sections: List<Section> = emptyList(),
    val groups: List<AppGroup> = emptyList(),
    /** How many [groups] come before the first letter ("New"), so the rail's entries match the list's items. */
    val railLead: Int = 0,
    val results: List<SearchHit> = emptyList(),
    /** The query [results] were built for; null while a source is still searching for the one typed. */
    val resultsQuery: String? = null,
    val iconMode: IconMode = IconMode.Colour,
    /** The chosen icon pack; part of [com.wander.mono.ui.components.AppIcons] so icons reload on a change. */
    val iconPack: String = "",
    val hiddenLocked: Boolean = false,
    val privateSpace: PrivateSpaceState = PrivateSpaceState.Absent,
    val privateApps: List<DrawerEntry> = emptyList(),
    val work: List<WorkSection> = emptyList(),
    val look: DrawerLook = DrawerLook(),
    val railAtStart: Boolean = false,
    val controls: ListControls = ListControls(),
)

/** What the drawer is drawn on, and what its scrim needs to keep text legible over the wallpaper. */
data class DrawerLook(
    val background: DrawerBackground = DrawerBackground.SoftBlur,
    val tone: WallpaperTone? = null,
    val highContrast: Boolean = false,
)
