package com.wander.mono.ui.drawer

/**
 * A pasted address or text in the query: a web address to open ("example.org/page", with or without
 * `https://`), and text long enough to be pasted rather than typed, to share. Only `http` and
 * `https` count as links; any other scheme (`intent:`, `javascript:`) is left as text.
 */
internal object SearchLinks {
    /** Shorter text is a name being typed, not something pasted to pass on. */
    const val SHARE_MIN_LENGTH = 20

    private val webScheme = Regex("^https?://[^/?#\\s]+.*", RegexOption.IGNORE_CASE)
    private val bareHost = Regex("^([a-z0-9-]+\\.)+[a-z]{2,24}(:\\d{1,5})?$", RegexOption.IGNORE_CASE)

    /** [query] as a web address to open, `https://` added when it has no scheme; null when it is not one. */
    fun urlOf(query: String): String? {
        val text = query.trim()
        val host = text.substringBefore('/').substringBefore('?').substringBefore('#')
        return when {
            text.isEmpty() || text.any { it.isWhitespace() } -> null
            webScheme.matches(text) -> text
            "://" in text || '@' in text -> null
            bareHost.matches(host) -> "https://$text"
            else -> null
        }
    }

    /** [query] trimmed when it is worth sharing: a link, or text long enough to have been pasted. */
    fun shareableOf(query: String): String? {
        val text = query.trim()
        val pasted = text.length >= SHARE_MIN_LENGTH && text.any { it.isWhitespace() }
        return text.takeIf { pasted || urlOf(it) != null }
    }

    /** What [query] offers to hand off: open it as a link, share it; either, both or neither. */
    fun of(query: String): List<SearchHit> = listOfNotNull(
        urlOf(query)?.let { SearchHit.Link(it) },
        shareableOf(query)?.let { SearchHit.ShareText(it) },
    )
}
