package com.wander.mono.data.settings

object BadgeSettings {
    /** Mark apps with their unread count; off, a plain dot. */
    val ShowCounts = BooleanSetting("badge_show_counts", default = true)
}
