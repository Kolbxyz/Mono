package com.wander.mono.data.settings

/** Whether and how the now-playing player under the clock shows on home. */
object MediaSettings {
    /** The player shows under the clock while something plays. */
    val ShowPlayer = BooleanSetting("media_show_player", default = true)

    /** The player opens in full instead of as its one-line strip. */
    val ExpandedByDefault = BooleanSetting("media_expanded_by_default", default = false)

    /** A sideways swipe on the player skips to the next or previous track. */
    val SwipeToSkip = BooleanSetting("media_swipe_to_skip", default = true)

    /** The player takes on the artwork's colour. */
    val ArtTint = BooleanSetting("media_art_tint", default = true)

    /** How long a paused session keeps its player (spec 7.6). */
    val PausedHideAfter = EnumSetting("media_paused_hide_after", PausedHide.TenMinutes, PausedHide.entries)
}

/** How long a paused session keeps its player; [millis] null keeps it until the session goes away. */
enum class PausedHide(override val stored: String, val millis: Long?) : StoredEnum {
    FiveMinutes("5m", FIVE_MINUTES_MS),
    TenMinutes("10m", TEN_MINUTES_MS),
    ThirtyMinutes("30m", THIRTY_MINUTES_MS),
    Never("never", null),
}

private const val FIVE_MINUTES_MS = 300_000L
private const val TEN_MINUTES_MS = 600_000L
private const val THIRTY_MINUTES_MS = 1_800_000L
