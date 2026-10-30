package com.wander.mono.data.settings

/** How the now-playing card behaves on home. */
object MediaSettings {
    /** The card opens as the full player instead of its one-line form. */
    val ExpandedByDefault = BooleanSetting("media_expanded_by_default", default = false)

    /** A sideways swipe on the card skips to the next or previous track. */
    val SwipeToSkip = BooleanSetting("media_swipe_to_skip", default = true)

    /** The card takes on the artwork's colour. */
    val ArtTint = BooleanSetting("media_art_tint", default = true)

    /** How long a paused session keeps its card (spec 7.6). */
    val PausedHideAfter = EnumSetting("media_paused_hide_after", PausedHide.TenMinutes, PausedHide.entries)
}

/** How long a paused session keeps its card; [millis] null keeps it until the session goes away. */
enum class PausedHide(override val stored: String, val millis: Long?) : StoredEnum {
    FiveMinutes("5m", 5 * MINUTE_MS),
    TenMinutes("10m", 10 * MINUTE_MS),
    ThirtyMinutes("30m", 30 * MINUTE_MS),
    Never("never", null),
}

private const val MINUTE_MS = 60_000L
