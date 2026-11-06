package com.wander.mono.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class BesidePreviewTest {
    @Test
    fun `a lone notification shown as text needs no count`() {
        assertEquals(AppBadge.None, AppBadge.Count(1).besidePreview(previewShown = true))
    }

    @Test
    fun `two or more still count beside the text`() {
        assertEquals(AppBadge.Count(2), AppBadge.Count(2).besidePreview(previewShown = true))
    }

    @Test
    fun `without its text, even one notification keeps its count, and dots stay dots`() {
        assertEquals(AppBadge.Count(1), AppBadge.Count(1).besidePreview(previewShown = false))
        assertEquals(AppBadge.Dot, AppBadge.Dot.besidePreview(previewShown = true))
    }
}
