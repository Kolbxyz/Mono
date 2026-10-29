package com.wander.mono.data.settings

object QuietSettings {
    /** Home shows only the quiet apps, without badges or previews; the drawer asks before opening. */
    val Enabled = BooleanSetting("quiet_enabled", default = false)

    /** Turn on Do Not Disturb (through Mono's own Android mode) together with quiet mode. */
    val LinkDoNotDisturb = BooleanSetting("quiet_link_dnd", default = false)
}
