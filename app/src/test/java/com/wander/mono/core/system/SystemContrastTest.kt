package com.wander.mono.core.system

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemContrastTest {
    @Test
    fun `standard and reduced contrast are not raised`() {
        assertFalse(isRaisedContrast(0f))
        assertFalse(isRaisedContrast(-1f))
    }

    @Test
    fun `medium and high contrast are raised`() {
        assertTrue(isRaisedContrast(0.5f))
        assertTrue(isRaisedContrast(1f))
    }
}
