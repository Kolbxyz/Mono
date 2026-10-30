package com.wander.mono.core.calendar

/**
 * The first video-meeting link in [text] (an event's location or description), null when there is
 * none. Only links to known meeting services count, so an event's ordinary web links never become
 * a Join button. Trailing punctuation the sentence put after the link is left out.
 */
internal fun meetingLinkIn(text: String?): String? {
    if (text.isNullOrBlank()) return null
    return LINK.findAll(text)
        .map { it.value.trimEnd(*TRAILING) }
        .firstOrNull { link -> hostOf(link)?.let(::isMeetingHost) == true }
}

/** The lower-cased host of an http(s) [link], without port or user info. */
private fun hostOf(link: String): String? {
    val afterScheme = link.substringAfter("://", missingDelimiterValue = "")
    val authority = afterScheme.substringBefore('/').substringBefore('?').substringBefore('#')
    return authority.substringAfterLast('@').substringBefore(':').lowercase().ifEmpty { null }
}

private fun isMeetingHost(host: String): Boolean =
    MEETING_HOSTS.any { host == it || host.endsWith(".$it") }

private val LINK = Regex("""https?://[^\s<>"']+""", RegexOption.IGNORE_CASE)
private val TRAILING = charArrayOf('.', ',', ';', ':', ')', ']', '>', '!', '?')

/** Video-meeting services, each with its subdomains (a company's `acme.zoom.us`, `acme.webex.com`). */
private val MEETING_HOSTS = listOf(
    "meet.google.com",
    "zoom.us",
    "zoomgov.com",
    "teams.microsoft.com",
    "teams.live.com",
    "meet.jit.si",
    "whereby.com",
    "webex.com",
    "meet.goto.com",
    "gotomeeting.com",
    "bluejeans.com",
    "chime.aws",
    "meet.proton.me",
)
