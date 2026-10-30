package com.wander.mono.data.settings

/** How app rows show icons: the original icons, monochrome icons tinted to the theme, or none. */
enum class IconMode(override val stored: String) : StoredEnum {
    Colour("colour"),
    Themed("themed"),
    TextOnly("text_only"),
}

/** The outline app icons are cut to: the system's own, or one of Material's shapes. */
enum class IconShape(override val stored: String) : StoredEnum {
    System("system"),
    Circle("circle"),
    Squircle("squircle"),
    Cookie("cookie"),
    Clover("clover"),
}

/**
 * The colour themed (monochrome) icons are drawn in: the theme's quiet neutral, the app's own
 * colour from its full-colour icon, or one of the wallpaper scheme's accent roles.
 */
enum class ThemedIconTint(override val stored: String) : StoredEnum {
    Neutral("neutral"),
    App("app"),
    Primary("primary"),
    Secondary("secondary"),
    Tertiary("tertiary"),
}

object IconSettings {
    val Mode = EnumSetting("icon_mode", IconMode.Colour, IconMode.entries)

    /** The icon pack's package name, used in [IconMode.Colour]; empty for the apps' own icons. */
    val Pack = StringSetting("icon_pack", default = SYSTEM_ICON_PACK)

    /** Applies to full-colour icons whose app ships an adaptive icon; [IconShape.System] changes nothing. */
    val Shape = EnumSetting("icon_shape", IconShape.System, IconShape.entries)

    /** Applies to [IconMode.Themed]; [ThemedIconTint.Neutral] is the original look. */
    val ThemedTint = EnumSetting("icon_themed_tint", ThemedIconTint.Neutral, ThemedIconTint.entries)

    /** Rows take a touch of their app's colour (the accent read from its icon). */
    val TintRows = BooleanSetting("icon_tint_rows", default = false)
}

/** How icons and rows are coloured, read together for `MonoTheme`'s `LocalIconTheming`. */
data class IconTheming(val themedTint: ThemedIconTint, val tintRows: Boolean) {
    companion object {
        /** Both settings' defaults: the look before either existed. */
        val Default = IconTheming(IconSettings.ThemedTint.default, IconSettings.TintRows.default)
    }
}

/** [IconSettings.Pack] when no pack is chosen. */
const val SYSTEM_ICON_PACK = ""

