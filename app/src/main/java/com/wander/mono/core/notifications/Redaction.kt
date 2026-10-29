package com.wander.mono.core.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService.Ranking

/** How much of a notification a preview may show. */
enum class Redaction {
    /** Title and text as posted. */
    Full,

    /** The app's own public version (often "New message"). */
    Public,

    /** Nothing of the content: only that there is something. */
    Hidden,
}

/**
 * The preview follows the lockscreen rules the app and the user set. Secret (from the app, or
 * forced by the user's per-channel override) shows no content at all. Private content is fine on
 * an unlocked home, except for a profile that is itself locked or paused: then only the public
 * version shows, or nothing when the app gave none.
 */
internal fun redactionFor(
    visibility: Int,
    userOverride: Int,
    profileLocked: Boolean,
    hasPublicVersion: Boolean,
): Redaction {
    val effective = if (userOverride == Ranking.VISIBILITY_NO_OVERRIDE) {
        visibility
    } else {
        minOf(visibility, userOverride)
    }
    return when {
        effective <= Notification.VISIBILITY_SECRET -> Redaction.Hidden
        effective == Notification.VISIBILITY_PRIVATE && profileLocked ->
            if (hasPublicVersion) Redaction.Public else Redaction.Hidden
        else -> Redaction.Full
    }
}
