package com.wander.mono.ui.onboarding

import com.wander.mono.ui.onboarding.OnboardingStep.DefaultHome
import com.wander.mono.ui.onboarding.OnboardingStep.Done
import com.wander.mono.ui.onboarding.OnboardingStep.NotificationAccess
import com.wander.mono.ui.onboarding.OnboardingStep.Welcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OnboardingStepTest {
    private val nothingSet = SetupFacts(isDefaultHome = false, hasNotificationAccess = false)
    private val homeSet = SetupFacts(isDefaultHome = true, hasNotificationAccess = false)
    private val allSet = SetupFacts(isDefaultHome = true, hasNotificationAccess = true)

    @Test
    fun `with nothing set up every step is shown in order and done ends it`() {
        assertEquals(DefaultHome, stepAfter(Welcome, nothingSet))
        assertEquals(NotificationAccess, stepAfter(DefaultHome, nothingSet))
        assertEquals(Done, stepAfter(NotificationAccess, nothingSet))
        assertNull(stepAfter(Done, nothingSet))
    }

    @Test
    fun `steps the system already reports done are passed over`() {
        assertEquals(NotificationAccess, stepAfter(Welcome, homeSet))
        assertEquals(Done, stepAfter(Welcome, allSet))
    }

    @Test
    fun `granting from the system screen moves the shown step on`() {
        assertEquals(DefaultHome, stepToShow(DefaultHome, nothingSet))
        assertEquals(NotificationAccess, stepToShow(DefaultHome, homeSet))
        assertEquals(Done, stepToShow(NotificationAccess, allSet))
        assertEquals(Welcome, stepToShow(Welcome, allSet))
    }

    @Test
    fun `back goes to the nearest earlier step still to do`() {
        assertEquals(DefaultHome, stepBefore(NotificationAccess, nothingSet))
        assertEquals(Welcome, stepBefore(NotificationAccess, homeSet))
        assertEquals(Welcome, stepBefore(Done, allSet))
        assertNull(stepBefore(Welcome, nothingSet))
    }
}
