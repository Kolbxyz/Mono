package com.wander.mono.data.settings

/** How tall the favourite rows are (spec 4.3): their height, icon and label grow together. */
enum class RowDensity(override val stored: String) : StoredEnum {
    Compact("compact"),
    Default("default"),
    Comfortable("comfortable"),
    ;

    /** The next density, back to the first after the last, for edit mode's Density button. */
    fun next(): RowDensity = entries[(ordinal + 1) % entries.size]
}

object HomeLookSettings {
    /** A shadow under home text instead of the surface scrim, for users who want the wallpaper untouched. */
    val TextShadow = BooleanSetting("home_text_shadow", default = false)

    val Density = EnumSetting("home_row_density", RowDensity.Default, RowDensity.entries)
}
