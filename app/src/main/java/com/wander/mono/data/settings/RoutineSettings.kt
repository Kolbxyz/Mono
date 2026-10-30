package com.wander.mono.data.settings

object RoutineSettings {
    /** During each routine's window, its favourites go first on home. Off until the user wants it. */
    val Enabled = BooleanSetting("routines_enabled", default = false)
}
