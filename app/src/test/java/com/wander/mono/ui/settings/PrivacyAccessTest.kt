package com.wander.mono.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyAccessTest {
    @Test
    fun `android 15 and later list every access`() {
        assertEquals(PrivacyAccess.entries, accessesOn(35))
        assertEquals(PrivacyAccess.entries, accessesOn(37))
    }

    @Test
    fun `hidden profiles only exist from android 15`() {
        assertFalse(PrivacyAccess.HiddenProfiles in accessesOn(34))
        assertTrue(PrivacyAccess.DoNotDisturb in accessesOn(34))
    }

    @Test
    fun `do not disturb modes need android 10`() {
        assertFalse(PrivacyAccess.DoNotDisturb in accessesOn(28))
        assertTrue(PrivacyAccess.Notifications in accessesOn(26))
    }
}
