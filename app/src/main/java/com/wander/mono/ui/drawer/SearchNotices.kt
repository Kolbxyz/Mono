package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.core.notifications.NotificationPreview
import com.wander.mono.core.notifications.PreviewConversation
import com.wander.mono.core.notifications.Redaction

/**
 * Posted notifications whose title or text match the query. Only what the home previews may show is
 * searched: a redacted notification is matched on its public version, a hidden one not at all, and
 * an app search leaves out (a hidden app while locked) leaves its notifications out too.
 */
internal object SearchNotices {
    const val MAX_NOTICES = 4

    /** The best matches among [previews], for apps in [apps] only, best first, at most [MAX_NOTICES]. */
    fun find(query: String, previews: List<NotificationPreview>, apps: List<DrawerEntry>): List<SearchHit.Notice> {
        val normalized = AppSearch.normalize(query)
        if (normalized.isEmpty() || previews.isEmpty()) return emptyList()
        val byOwner = apps.associateBy { it.app.key.packageName to it.app.key.userSerial }
        return previews
            .flatMap { preview ->
                val app = byOwner[preview.owner.packageName to preview.owner.userSerial]?.app
                if (app == null) emptyList() else candidatesOf(preview, app)
            }
            .mapNotNull { it.matching(normalized) }
            .sortedBy { it.second }
            .take(MAX_NOTICES)
            .map { it.first }
    }

    /** One per conversation of the app's expanded preview, or the preview itself when it has none. */
    private fun candidatesOf(preview: NotificationPreview, app: LaunchableApp): List<Candidate> {
        val conversations = preview.expansion.conversations.filter { it.redaction != Redaction.Hidden }
        return when {
            conversations.isNotEmpty() -> conversations.map { Candidate(preview, it, app, it.title, lastLineOf(it)) }
            preview.redaction == Redaction.Hidden -> emptyList()
            else -> listOf(Candidate(preview, null, app, preview.title, preview.text))
        }
    }

    /** The conversation's latest message, after its sender when it has one ("Léa: on my way"). */
    private fun lastLineOf(conversation: PreviewConversation): String? = conversation.messages.lastOrNull()?.let {
        if (it.sender == null) it.text else "${it.sender}: ${it.text}"
    }

    /**
     * A notification as it may show. Without a [title] of its own the row is titled with the app's
     * name, which is never matched: the app itself is already a result for its name.
     */
    private class Candidate(
        val preview: NotificationPreview,
        val conversation: PreviewConversation?,
        val app: LaunchableApp,
        val title: String?,
        val text: String?,
    ) {
        /** The hit with what matched, and its best tier; null when neither title nor text match. */
        fun matching(query: String): Pair<SearchHit.Notice, Int>? {
            val inTitle = title?.let { FoldedText(it).match(query) }
            val inText = text?.let { FoldedText(it).match(query) }
            val tier = listOfNotNull(inTitle?.tier, inText?.tier).minOrNull() ?: return null
            val hit = SearchHit.Notice(
                preview = preview,
                conversation = conversation,
                app = app,
                title = title ?: app.label,
                text = text,
                matched = inTitle?.ranges.orEmpty(),
                textMatched = inText?.ranges.orEmpty(),
            )
            return hit to tier
        }
    }
}
