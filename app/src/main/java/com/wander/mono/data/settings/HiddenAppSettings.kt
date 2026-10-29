package com.wander.mono.data.settings

object HiddenAppSettings {
    /** Ask for the screen lock before hidden apps show in search or in settings. */
    val Lock = BooleanSetting("hidden_lock", default = false)
}
