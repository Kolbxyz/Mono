package com.wander.mono.core.apps

/**
 * How app rows show icons: the original icons, monochrome icons tinted to the theme, or none.
 * [stored] is the persisted form, fixed so constants can be renamed without losing the choice.
 */
enum class IconMode(val stored: String) {
    Colour("colour"),
    Themed("themed"),
    TextOnly("text_only"),
    ;

    companion object {
        val Default = Colour

        /** The stored form back to a mode; anything unknown (or nothing stored) is [Default]. */
        fun fromStored(stored: String?): IconMode = entries.firstOrNull { it.stored == stored } ?: Default
    }
}
