package com.wander.mono.data.settings

object UsageSettings {
    /** Show today's time in an app in its long-press options; needs usage access. */
    val ShowInSheet = BooleanSetting("usage_in_sheet", default = false)
}
