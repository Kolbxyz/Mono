package com.wander.mono.data.settings

object QuietSettings {
    /** Home shows only the quiet apps, without badges or previews; the drawer asks before opening. */
    val Enabled = BooleanSetting("quiet_enabled", default = false)

    /** Turn on Do Not Disturb (through Mono's own Android mode) together with quiet mode. */
    val LinkDoNotDisturb = BooleanSetting("quiet_link_dnd", default = false)

    /** When quiet mode ends by itself (epoch millis), set from the clock's Quiet chip; 0 for never. */
    val Until = LongSetting("quiet_until", default = 0L)
}
