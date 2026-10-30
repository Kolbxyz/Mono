package com.wander.mono.core.notifications

import android.app.PendingIntent
import android.app.RemoteInput

/**
 * The latest notification of an app, as its home preview row shows it. [title] and [text] are
 * null when [redaction] is [Redaction.Hidden]: the row then only says there is something new.
 */
data class NotificationPreview(
    /** `StatusBarNotification.key`, what dismissing cancels. */
    val key: String,
    val owner: PackageUser,
    val title: String?,
    val text: String?,
    val redaction: Redaction,
    /** What tapping the notification in the shade opens; null when the app gave nothing. */
    val contentIntent: PendingIntent?,
    /** Cancelled once opened, as the shade does for `FLAG_AUTO_CANCEL`. */
    val autoCancel: Boolean,
    /** False for notifications the user cannot swipe away in the shade either. */
    val isClearable: Boolean,
    val reply: ReplyAction?,
    /** What the row shows once expanded. */
    val expansion: PreviewExpansion,
)

/** A notification action that takes free-form text, such as a messenger's "Reply". */
data class ReplyAction(
    val label: String,
    val intent: PendingIntent,
    val remoteInputs: List<RemoteInput>,
    /** The free-form input the typed text goes to. */
    val resultKey: String,
    /** The input's own hint ("Message Léa"), null when the app gave none. */
    val hint: String?,
)
