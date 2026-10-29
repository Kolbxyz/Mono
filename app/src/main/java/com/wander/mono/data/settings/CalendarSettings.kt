package com.wander.mono.data.settings

object CalendarSettings {
    /** Show upcoming events on home. Off by default: it needs the calendar permission. */
    val Enabled = BooleanSetting("calendar_enabled", default = false)
}
