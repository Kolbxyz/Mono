package com.wander.mono.data.settings

object ScreenTimeSettings {
    /** Dim a favourite on home once today's time in it passes its daily limit; needs usage access. */
    val Nudge = BooleanSetting("screen_time_nudge", default = false)
}
