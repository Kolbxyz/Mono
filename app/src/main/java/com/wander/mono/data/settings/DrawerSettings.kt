package com.wander.mono.data.settings

/**
 * What the drawer sits on: the home scrim over the wallpaper, the wallpaper blurred (Android 12+,
 * when the device allows window blur; the scrim otherwise), or the opaque surface.
 */
enum class DrawerBackground(override val stored: String) : StoredEnum {
    Scrim("scrim"),
    Blur("blur"),
    Solid("solid"),
}

object DrawerSettings {
    val Background = EnumSetting("drawer_background", DrawerBackground.Scrim, DrawerBackground.entries)

    /** Every opening of the drawer focuses search and brings up the keyboard, not only home's search bar. */
    val KeyboardOnOpen = BooleanSetting("drawer_keyboard_on_open", default = false)

    /**
     * The letter rail on the start edge instead of the end: the left in a left-to-right language,
     * the right in a right-to-left one. For the other hand's thumb.
     */
    val RailAtStart = BooleanSetting("drawer_rail_start", default = false)
}
