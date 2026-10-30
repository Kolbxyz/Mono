package com.wander.mono.core.notifications

import android.app.PendingIntent

/**
 * An expanded preview row (spec 7.7): the app's latest conversations, the latest notification's own
 * buttons, and every notification of the app that Mark read and Mute reach.
 */
data class PreviewExpansion(
    /** Newest first, at most [MAX_CONVERSATIONS]. */
    val conversations: List<PreviewConversation>,
    /** Without reply inputs (the reply field takes those) or mark-as-read (Mark read fires it). */
    val actions: List<PreviewAction>,
    val handles: List<NotificationHandle>,
)

/** One conversation (a room, or a notification on its own). [messages] are empty when [redaction] hides them. */
data class PreviewConversation(
    /** `StatusBarNotification.key` of its latest notification. */
    val key: String,
    val title: String?,
    /** Its last [MAX_MESSAGES] messages, oldest first. */
    val messages: List<PreviewMessage>,
    val redaction: Redaction,
    /** What tapping its title opens: the deep link into the room; null when the app gave none. */
    val contentIntent: PendingIntent?,
    val autoCancel: Boolean,
    /** The app it came from, whose lock holds opening it. */
    val owner: PackageUser,
)

/** [sender] is null for a plain notification's text, which has none. */
data class PreviewMessage(val sender: String?, val text: String)

/** A button the notification itself offers ("Archive", "Like"), fired as the shade would; [owner]'s lock holds it. */
data class PreviewAction(val label: String, val intent: PendingIntent, val owner: PackageUser)

/** One notification as Mark read and Mute act on it. */
data class NotificationHandle(
    val key: String,
    /** The app's own `SEMANTIC_ACTION_MARK_AS_READ` action; null when it offers none. */
    val markRead: PendingIntent?,
    val isClearable: Boolean,
)

const val MAX_CONVERSATIONS = 3
const val MAX_MESSAGES = 2
