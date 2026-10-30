package com.wander.mono.core.notifications

import android.app.Notification
import android.app.PendingIntent
import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat

/**
 * One conversation of an expanded row under [redaction]: a `MessagingStyle` conversation shows its
 * title over its last messages, anything else its title over its full text. Public shows the app's
 * public version, Hidden nothing at all.
 */
internal fun conversationOf(sbn: StatusBarNotification, owner: PackageUser, redaction: Redaction): PreviewConversation {
    val notification = sbn.notification
    val (title, messages) = when (redaction) {
        Redaction.Full -> messagesOf(notification)
        Redaction.Public -> notification.publicVersion?.let(::plainMessagesOf) ?: (null to emptyList())
        Redaction.Hidden -> null to emptyList()
    }
    return PreviewConversation(
        key = sbn.key,
        title = title,
        messages = messages,
        redaction = if (title == null && messages.isEmpty()) Redaction.Hidden else redaction,
        contentIntent = notification.contentIntent,
        autoCancel = notification.flags and Notification.FLAG_AUTO_CANCEL != 0,
        owner = owner,
    )
}

/** [sbn] as Mark read and Mute reach it. */
internal fun handleOf(sbn: StatusBarNotification) = NotificationHandle(
    key = sbn.key,
    markRead = sbn.notification.actions.orEmpty().firstOrNull { it.isMarkRead() }?.actionIntent,
    isClearable = sbn.isClearable,
)

/**
 * The buttons [notification] offers that work with a plain tap: not those taking input (the reply
 * field covers replying), not the system's contextual suggestions, not mark-as-read (Mark read).
 */
internal fun buttonsOf(notification: Notification, owner: PackageUser): List<PreviewAction> =
    notification.actions.orEmpty().mapNotNull { action ->
        val intent: PendingIntent = action.actionIntent ?: return@mapNotNull null
        val label = action.title?.toString()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val contextual = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && action.isContextual
        val takesInput = !action.remoteInputs.isNullOrEmpty()
        if (contextual || takesInput || action.isMarkRead()) null else PreviewAction(label, intent, owner)
    }

private fun Notification.Action.isMarkRead(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
        semanticAction == Notification.Action.SEMANTIC_ACTION_MARK_AS_READ &&
        actionIntent != null && remoteInputs.isNullOrEmpty()

private fun messagesOf(notification: Notification): Pair<String?, List<PreviewMessage>> {
    val style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
    val messages = style?.let(::lastMessagesOf).orEmpty()
    if (style == null || messages.isEmpty()) return plainMessagesOf(notification)
    val title = style.conversationTitle?.toString()
        ?: notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        ?: messages.last().sender
    return title to messages
}

/** The last messages of [style]; a message without a sender is the user's own (a reply shown in the thread). */
private fun lastMessagesOf(style: NotificationCompat.MessagingStyle): List<PreviewMessage> =
    style.messages.mapNotNull { message ->
        val text = message.text?.toString()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        PreviewMessage((message.person ?: style.user).name?.toString(), text)
    }.takeLast(MAX_MESSAGES)

private fun plainMessagesOf(notification: Notification): Pair<String?, List<PreviewMessage>> {
    val extras = notification.extras
    val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.takeIf { it.isNotBlank() }
    val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: extras.getCharSequence(Notification.EXTRA_TEXT))
        ?.toString()?.takeIf { it.isNotBlank() }
    return title to listOfNotNull(text?.let { PreviewMessage(sender = null, text = it) })
}
