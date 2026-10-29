package com.wander.mono.data.settings

/** How app rows show icons: the original icons, monochrome icons tinted to the theme, or none. */
enum class IconMode(override val stored: String) : StoredEnum {
    Colour("colour"),
    Themed("themed"),
    TextOnly("text_only"),
}

object IconSettings {
    val Mode = EnumSetting("icon_mode", IconMode.Colour, IconMode.entries)

    /** The icon pack's package name, used in [IconMode.Colour]; empty for the apps' own icons. */
    val Pack = StringSetting("icon_pack", default = SYSTEM_ICON_PACK)
}

/** [IconSettings.Pack] when no pack is chosen. */
const val SYSTEM_ICON_PACK = ""

