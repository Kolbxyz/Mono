package com.wander.mono.data.settings

object ScreenTimeSettings {
    /**
     * Screen time, reading usage access: today's time in an app's long-press options, and a favourite
     * dimmed on home once that time passes its daily limit.
     */
    val Nudge = BooleanSetting("screen_time_nudge", default = false)
}
