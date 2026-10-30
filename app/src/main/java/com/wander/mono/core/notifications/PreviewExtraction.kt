package com.wander.mono.core.notifications

import android.app.Notification
import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat

/**
 * Reads the preview of [sbn] under [redaction]. A conversation (`MessagingStyle`) shows its title
 * (or the sender, one to one) over its latest message; anything else shows its title and text.
 * Replying is only offered when the full content is shown.
 */
internal fun previewOf(
    sbn: StatusBarNotification,
    owner: PackageUser,
    redaction: Redaction,
    expansion: PreviewExpansion,
): NotificationPreview {
    val notification = sbn.notification
    val (title, text) = when (redaction) {
        Redaction.Full -> contentOf(notification)
        Redaction.Public -> notification.publicVersion?.let(::plainContentOf) ?: (null to null)
        Redaction.Hidden -> null to null
    }
    return NotificationPreview(
        key = sbn.key,
        owner = owner,
        title = title,
        text = text,
        redaction = if (title == null && text == null) Redaction.Hidden else redaction,
        contentIntent = notification.contentIntent,
        autoCancel = notification.flags and Notification.FLAG_AUTO_CANCEL != 0,
        isClearable = sbn.isClearable,
        reply = if (redaction == Redaction.Full) replyActionOf(notification) else null,
        expansion = expansion,
    )
}

/**
 * Unread messages [notification] stands for: its `number` when the app sets one, else the messages
 * of a conversation not sent by the user, else 1.
 */
internal fun unreadOf(notification: Notification): Int {
    if (notification.number > 0) return notification.number
    val style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
    // A message without a sender is the user's own (a reply shown in the thread).
    val fromOthers = style?.messages?.count { message ->
        val sender = message.person ?: return@count false
        sender.key == null || sender.key != style.user.key
    } ?: 0
    return fromOthers.coerceAtLeast(1)
}

private fun contentOf(notification: Notification): Pair<String?, String?> {
    val style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
    val last = style?.messages?.lastOrNull() ?: return plainContentOf(notification)
    val sender = last.person?.name?.toString()
    val group = style.conversationTitle?.toString()
    return if (group != null && sender != null) {
        group to "$sender: ${last.text}"
    } else {
        (group ?: sender ?: notification.extras.text(Notification.EXTRA_TITLE)) to last.text?.toString()
    }
}

private fun plainContentOf(notification: Notification): Pair<String?, String?> {
    val extras = notification.extras
    return extras.text(Notification.EXTRA_TITLE) to
        (extras.text(Notification.EXTRA_TEXT) ?: extras.text(Notification.EXTRA_BIG_TEXT))
}

private fun android.os.Bundle.text(key: String): String? = getCharSequence(key)?.toString()?.takeIf { it.isNotBlank() }

/** The action taking free-form text, preferring the one the app marks as "reply". */
private fun replyActionOf(notification: Notification): ReplyAction? {
    val candidates = notification.actions.orEmpty().filter { action ->
        action.actionIntent != null && action.remoteInputs.orEmpty().any { it.allowFreeFormInput }
    }
    val action = candidates.firstOrNull {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && it.semanticAction == Notification.Action.SEMANTIC_ACTION_REPLY
    } ?: candidates.firstOrNull() ?: return null
    val inputs = action.remoteInputs.orEmpty().toList()
    val input = inputs.first { it.allowFreeFormInput }
    return ReplyAction(
        label = action.title?.toString().orEmpty(),
        intent = action.actionIntent,
        remoteInputs = inputs,
        resultKey = input.resultKey,
        hint = input.label?.toString()?.takeIf { it.isNotBlank() },
    )
}
