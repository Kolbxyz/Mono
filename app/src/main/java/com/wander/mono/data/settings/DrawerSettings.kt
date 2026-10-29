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
}
