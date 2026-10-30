package com.wander.mono.data.settings

/**
 * What the drawer sits on: the bare wallpaper; the wallpaper softly blurred under a light tint, or
 * strongly blurred under the home scrim (Android 12+, when the device allows window blur; the tint
 * alone otherwise); the home scrim over the wallpaper; or the opaque surface.
 */
enum class DrawerBackground(override val stored: String) : StoredEnum {
    None("none"),
    SoftBlur("soft_blur"),
    Scrim("scrim"),
    Blur("blur"),
    Solid("solid"),
}

/** How the drawer's list is ordered: by name under the letter rail, or newest install first. */
enum class DrawerSort(override val stored: String) : StoredEnum {
    Alphabetical("alphabetical"),
    Recent("recent"),
}

object DrawerSettings {
    val Background = EnumSetting("drawer_background", DrawerBackground.SoftBlur, DrawerBackground.entries)

    /** Every opening of the drawer focuses search and brings up the keyboard, not only home's search bar. */
    val KeyboardOnOpen = BooleanSetting("drawer_keyboard_on_open", default = false)

    /**
     * The letter rail on the start edge instead of the end: the left in a left-to-right language,
     * the right in a right-to-left one. For the other hand's thumb.
     */
    val RailAtStart = BooleanSetting("drawer_rail_start", default = false)

    /** Apps installed in the last few days, until first opened, also in a "New" group at the top. */
    val NewApps = BooleanSetting("drawer_new_apps", default = true)

    /** Also switched from the drawer's own toolbar. */
    val Sort = EnumSetting("drawer_sort", DrawerSort.Alphabetical, DrawerSort.entries)
}
