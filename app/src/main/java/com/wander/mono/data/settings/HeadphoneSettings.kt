package com.wander.mono.data.settings

object HeadphoneSettings {
    /** While headphones are connected, the favourites chosen for it go to the top of home. */
    val LiftApps = BooleanSetting("headphones_lift_apps", default = true)
}
