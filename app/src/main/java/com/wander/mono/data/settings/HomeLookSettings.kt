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

    /** Home's row text in percent, on top of the system font size: it only ever grows. */
    val LabelScale = IntSetting("home_label_scale", default = 100, range = LABEL_SCALES)

    /** One-handed: the clock moves down above the search dock, so everything sits in thumb reach. */
    val Reachable = BooleanSetting("home_reachable_layout", default = false)

    /**
     * Where a list shorter than home sits, as a percent of the room left over that goes above it:
     * 0 at the top, 100 (the default) down in the thumb zone.
     */
    val ListPosition = IntSetting("home_list_position", default = LIST_POSITIONS.last, range = LIST_POSITIONS)
}

/** List positions in percent of the free room above the list. */
val LIST_POSITIONS = 0..100

/** Distance between the list position slider's stops, in percent. */
const val LIST_POSITION_STEP = 10

/** Label scales the slider stops at, in percent. */
val LABEL_SCALES = 100..150

/** Distance between the label scale slider's stops, in percent. */
const val LABEL_SCALE_STEP = 10
