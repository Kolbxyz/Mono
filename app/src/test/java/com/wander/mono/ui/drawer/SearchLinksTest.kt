package com.wander.mono.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchLinksTest {
    @Test
    fun `web addresses are links, https added when there is no scheme`() {
        assertEquals("https://example.org", SearchLinks.urlOf("example.org"))
        assertEquals("https://example.org", SearchLinks.urlOf("  example.org "))
        assertEquals("https://www.example.co.uk/page?q=1", SearchLinks.urlOf("www.example.co.uk/page?q=1"))
        assertEquals("https://example.org:8080/x", SearchLinks.urlOf("example.org:8080/x"))
        assertEquals("http://Example.org/a", SearchLinks.urlOf("http://Example.org/a"))
        assertEquals("HTTPS://example.org", SearchLinks.urlOf("HTTPS://example.org"))
    }

    @Test
    fun `names, numbers, sentences and other schemes are not links`() {
        assertNull(SearchLinks.urlOf("maps"))
        assertNull(SearchLinks.urlOf("3.14"))
        assertNull(SearchLinks.urlOf("1.5kg"))
        assertNull(SearchLinks.urlOf("hello world.com"))
        assertNull(SearchLinks.urlOf("me@example.org"))
        assertNull(SearchLinks.urlOf("javascript://alert(1)"))
        assertNull(SearchLinks.urlOf("intent:#Intent;end"))
        assertNull(SearchLinks.urlOf("file:///sdcard/a.txt"))
        assertNull(SearchLinks.urlOf("https://"))
        assertNull(SearchLinks.urlOf("localhost:8080"))
        assertNull(SearchLinks.urlOf(""))
    }

    @Test
    fun `long pasted text and links can be shared, short names cannot`() {
        assertEquals("This is some pasted text", SearchLinks.shareableOf(" This is some pasted text "))
        assertEquals("example.org", SearchLinks.shareableOf("example.org"))
        assertNull(SearchLinks.shareableOf("google maps"))
        assertNull(SearchLinks.shareableOf("averyveryverylongsingleword"))
    }

    @Test
    fun `a link offers both opening and sharing`() {
        assertEquals(
            listOf(SearchHit.Link("https://example.org"), SearchHit.ShareText("example.org")),
            SearchLinks.of("example.org"),
        )
        assertEquals(emptyList<SearchHit>(), SearchLinks.of("maps"))
    }
}
