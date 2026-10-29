package com.wander.mono.data.settings

object GestureSettings {
    /** Double-tapping an empty spot on home locks the screen (needs Mono's gesture service). */
    val DoubleTapLock = BooleanSetting("double_tap_lock", default = false)

    /** Swiping down on home opens the notification shade (needs Mono's gesture service). */
    val SwipeDownNotifications = BooleanSetting("swipe_down_notifications", default = true)
}
