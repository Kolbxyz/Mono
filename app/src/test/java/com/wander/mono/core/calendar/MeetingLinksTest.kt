package com.wander.mono.core.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MeetingLinksTest {
    @Test
    fun `finds a meeting link and drops the sentence's punctuation`() {
        assertEquals(
            "https://meet.google.com/abc-defg-hij",
            meetingLinkIn("Join here: https://meet.google.com/abc-defg-hij."),
        )
        assertEquals("https://acme.zoom.us/j/123?pwd=x", meetingLinkIn("(https://acme.zoom.us/j/123?pwd=x)"))
    }

    @Test
    fun `skips ordinary links for the first meeting one`() {
        val text = "Agenda https://example.com/doc then https://teams.microsoft.com/l/meetup-join/1"
        assertEquals("https://teams.microsoft.com/l/meetup-join/1", meetingLinkIn(text))
    }

    @Test
    fun `lookalike hosts and plain text are not meetings`() {
        assertNull(meetingLinkIn("https://notzoom.us/j/1"))
        assertNull(meetingLinkIn("https://zoom.us.evil.example/j/1"))
        assertNull(meetingLinkIn("Room 4, second floor"))
        assertNull(meetingLinkIn(null))
    }
}
