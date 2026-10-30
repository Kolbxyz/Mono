package com.wander.mono.ui.onboarding

import com.wander.mono.ui.onboarding.OnboardingStep.Calendar
import com.wander.mono.ui.onboarding.OnboardingStep.DefaultHome
import com.wander.mono.ui.onboarding.OnboardingStep.Done
import com.wander.mono.ui.onboarding.OnboardingStep.Favorites
import com.wander.mono.ui.onboarding.OnboardingStep.NotificationAccess
import com.wander.mono.ui.onboarding.OnboardingStep.Welcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OnboardingStepTest {
    private val nothingSet = SetupFacts(isDefaultHome = false, hasNotificationAccess = false)
    private val homeSet = SetupFacts(isDefaultHome = true, hasNotificationAccess = false)
    private val accessSet = SetupFacts(isDefaultHome = true, hasNotificationAccess = true)
    private val allSet = SetupFacts(isDefaultHome = true, hasNotificationAccess = true, showsCalendar = true)

    @Test
    fun `with nothing set up every step is shown in order and done ends it`() {
        assertEquals(DefaultHome, stepAfter(Welcome, nothingSet))
        assertEquals(Favorites, stepAfter(DefaultHome, nothingSet))
        assertEquals(NotificationAccess, stepAfter(Favorites, nothingSet))
        assertEquals(Calendar, stepAfter(NotificationAccess, nothingSet))
        assertEquals(Done, stepAfter(Calendar, nothingSet))
        assertNull(stepAfter(Done, nothingSet))
    }

    @Test
    fun `steps the system already reports done are passed over`() {
        assertEquals(Favorites, stepAfter(Welcome, homeSet))
        assertEquals(Calendar, stepAfter(Favorites, accessSet))
        assertEquals(Done, stepAfter(Favorites, allSet))
    }

    @Test
    fun `picking favourites is never passed over`() {
        assertEquals(Favorites, stepAfter(Welcome, allSet))
        assertEquals(Favorites, stepToShow(Favorites, allSet))
    }

    @Test
    fun `granting from the system screen moves the shown step on`() {
        assertEquals(DefaultHome, stepToShow(DefaultHome, nothingSet))
        assertEquals(Favorites, stepToShow(DefaultHome, homeSet))
        assertEquals(Calendar, stepToShow(NotificationAccess, accessSet))
        assertEquals(Done, stepToShow(Calendar, allSet))
        assertEquals(Welcome, stepToShow(Welcome, allSet))
    }

    @Test
    fun `back goes to the nearest earlier step still to do`() {
        assertEquals(Favorites, stepBefore(NotificationAccess, nothingSet))
        assertEquals(DefaultHome, stepBefore(Favorites, nothingSet))
        assertEquals(Welcome, stepBefore(Favorites, homeSet))
        assertEquals(NotificationAccess, stepBefore(Calendar, homeSet))
        assertEquals(Favorites, stepBefore(Done, allSet))
        assertNull(stepBefore(Welcome, nothingSet))
    }
}
