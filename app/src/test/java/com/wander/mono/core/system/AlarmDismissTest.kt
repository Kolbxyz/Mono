package com.wander.mono.core.system

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmDismissTest {
    private val hour = 60 * 60 * 1_000L

    @Test
    fun `dismiss is offered for an alarm ahead within a day`() {
        assertTrue(canOfferAlarmDismiss(alarm = 8 * hour, now = 0))
        assertTrue(canOfferAlarmDismiss(alarm = 24 * hour, now = 0))
    }

    @Test
    fun `not for no alarm, a past one or one further than a day`() {
        assertFalse(canOfferAlarmDismiss(alarm = null, now = 0))
        assertFalse(canOfferAlarmDismiss(alarm = 0, now = hour))
        assertFalse(canOfferAlarmDismiss(alarm = 25 * hour, now = 0))
    }
}
