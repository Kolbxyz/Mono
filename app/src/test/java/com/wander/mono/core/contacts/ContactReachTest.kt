package com.wander.mono.core.contacts

import org.junit.Assert.assertEquals
import org.junit.Test

class ContactReachTest {
    @Test
    fun `a number offers call and message, an address email`() {
        val both = Contact(1, "k", "Léa", phone = "+33 6 12 34 56 78", email = "lea@example.org")
        assertEquals(listOf(ContactReach.Call, ContactReach.Message, ContactReach.Email), reachOf(both))
        assertEquals(listOf(ContactReach.Call, ContactReach.Message), reachOf(both.copy(email = null)))
        assertEquals(listOf(ContactReach.Email), reachOf(both.copy(phone = null)))
    }

    @Test
    fun `a contact with neither offers nothing`() {
        assertEquals(emptyList<ContactReach>(), reachOf(Contact(1, "k", "Léa")))
    }
}
