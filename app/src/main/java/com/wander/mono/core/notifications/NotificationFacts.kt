package com.wander.mono.core.notifications

/** What [latestPerApp] and [unreadPerConversation] need to know about one active notification. */
internal data class NotificationFacts(
    /** `StatusBarNotification.key`. */
    val key: String,
    val owner: PackageUser,
    val postTime: Long,
    val isOngoing: Boolean,
    val isGroupSummary: Boolean,
    /** Importance below default: the user (or the app) made it silent. */
    val isSilent: Boolean,
    /** `Notification.shortcutId`: the conversation shortcut it belongs to, null when none. */
    val shortcutId: String?,
    /** Unread messages it stands for, at least 1. */
    val unread: Int,
)

/** One conversation of an app, named by the shortcut the app publishes for it (a Phalanx room). */
data class ConversationKey(val owner: PackageUser, val shortcutId: String)

/** News, not status: ongoing notifications and group summaries never get a preview or a count. */
private val NotificationFacts.isNews: Boolean get() = !isOngoing && !isGroupSummary

/** The key of each app's most recent non-silent notification, the one its preview row shows. */
internal fun latestPerApp(facts: List<NotificationFacts>): Map<PackageUser, String> = facts
    .filter { it.isNews && !it.isSilent }
    .groupBy { it.owner }
    .mapValues { (_, own) -> own.maxBy { it.postTime }.key }

/**
 * Unread messages per conversation, for notifications tied to a conversation shortcut. A messenger
 * may post one notification per room or several; either way they add up under the room's shortcut.
 */
internal fun unreadPerConversation(facts: List<NotificationFacts>): Map<ConversationKey, Int> = facts
    .filter { it.isNews && it.shortcutId != null }
    .groupingBy { ConversationKey(it.owner, it.shortcutId.orEmpty()) }
    .fold(0) { total, fact -> total + fact.unread.coerceAtLeast(1) }

/** [owner]'s news, silent or not: what an expanded row may show and what Mark read and Mute reach. */
internal fun newsOf(facts: List<NotificationFacts>, owner: PackageUser): List<NotificationFacts> =
    facts.filter { it.isNews && it.owner == owner }

/**
 * The notification to read for each conversation of [news], newest conversation first, at most
 * [limit]: a conversation is the notifications sharing a shortcut, or one notification on its own.
 */
internal fun conversationKeys(news: List<NotificationFacts>, limit: Int): List<String> = news
    .groupBy { it.shortcutId ?: it.key }
    .values
    .map { group -> group.maxBy { it.postTime } }
    .sortedByDescending { it.postTime }
    .take(limit)
    .map { it.key }
