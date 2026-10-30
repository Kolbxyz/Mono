package com.wander.mono.data.settings

/** What swiping up on home does: open the drawer, or open it with search focused and the keyboard up. */
enum class SwipeUpAction(override val stored: String) : StoredEnum {
    Drawer("drawer"),
    Search("search"),
}

/**
 * What swiping down on home does. The shade and quick settings need Mono's gesture service; search
 * opens the drawer with the keyboard up.
 */
enum class SwipeDownAction(override val stored: String) : StoredEnum {
    Notifications("notifications"),
    QuickSettings("quick_settings"),
    Search("search"),
    None("none"),
}

object GestureSettings {
    /** Double-tapping an empty spot on home locks the screen (needs Mono's gesture service). */
    val DoubleTapLock = BooleanSetting("double_tap_lock", default = false)

    val SwipeUp = EnumSetting("swipe_up_action", SwipeUpAction.Drawer, SwipeUpAction.entries)

    val SwipeDown = EnumSetting("swipe_down_action", SwipeDownAction.Notifications, SwipeDownAction.entries)

    /**
     * The on/off switch [SwipeDown] replaced, kept readable and in backups so older files keep their
     * choice. False is a choice not yet carried over: [swipeDownFromLegacy] turns it into
     * [SwipeDownAction.None], and this goes back to true once it has.
     */
    val LegacySwipeDownNotifications = BooleanSetting("swipe_down_notifications", default = true)
}

/** What [GestureSettings.SwipeDown] becomes for the old switch's value; null when there is nothing to carry over. */
fun swipeDownFromLegacy(notificationsOn: Boolean): SwipeDownAction? =
    if (notificationsOn) null else SwipeDownAction.None
