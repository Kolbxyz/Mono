package com.wander.mono.core.apps

import org.junit.Assert.assertEquals
import org.junit.Test

class AppCategoryTest {
    @Test
    fun `each declared category maps to its chip, in the platform's numbering`() {
        val expected = listOf(
            AppCategory.Games,
            AppCategory.Audio,
            AppCategory.Video,
            AppCategory.Image,
            AppCategory.Social,
            AppCategory.News,
            AppCategory.Maps,
            AppCategory.Productivity,
            AppCategory.Accessibility,
        )
        assertEquals(expected, (0..8).map(::appCategory))
    }

    @Test
    fun `undefined and unknown categories are Other`() {
        assertEquals(AppCategory.Other, appCategory(-1))
        assertEquals(AppCategory.Other, appCategory(42))
    }
}
