package com.wander.mono.ui.onboarding

/** First-run setup, in order. Every step can be skipped; Mono works with none of them done. */
enum class OnboardingStep { Welcome, DefaultHome, Favorites, NotificationAccess, Calendar, Done }

/**
 * What the system already says is set up, read again each time Mono comes back to the front.
 * [showsCalendar]: the calendar setting is on and the permission granted.
 */
data class SetupFacts(
    val isDefaultHome: Boolean,
    val hasNotificationAccess: Boolean,
    val showsCalendar: Boolean = false,
)

/**
 * A step whose job the system reports done needs no screen: Mono is already home, access already
 * granted. Favourites are a choice, never done for the user, so their step always shows.
 */
private fun OnboardingStep.satisfiedBy(facts: SetupFacts): Boolean = when (this) {
    OnboardingStep.DefaultHome -> facts.isDefaultHome
    OnboardingStep.NotificationAccess -> facts.hasNotificationAccess
    OnboardingStep.Calendar -> facts.showsCalendar
    OnboardingStep.Welcome, OnboardingStep.Favorites, OnboardingStep.Done -> false
}

/**
 * The step to show when [requested] is where the user is: it, or the first later one still to do.
 * So coming back from the system dialog having said yes moves straight on. Never past [OnboardingStep.Done].
 */
fun stepToShow(requested: OnboardingStep, facts: SetupFacts): OnboardingStep =
    OnboardingStep.entries.drop(requested.ordinal).first { !it.satisfiedBy(facts) }

/** Where Continue or Skip leads from [shown]; null from [OnboardingStep.Done], which ends setup. */
fun stepAfter(shown: OnboardingStep, facts: SetupFacts): OnboardingStep? =
    OnboardingStep.entries.getOrNull(shown.ordinal + 1)?.let { stepToShow(it, facts) }

/** Where Back leads from [shown]: the nearest earlier step still to do, or null at the first. */
fun stepBefore(shown: OnboardingStep, facts: SetupFacts): OnboardingStep? =
    OnboardingStep.entries.take(shown.ordinal).lastOrNull { !it.satisfiedBy(facts) }
