package com.wander.mono.ui.onboarding

import androidx.annotation.StringRes
import com.wander.mono.R

/** The main button's label on [step]; [retry] once notification access was asked for and is still off. */
@StringRes
internal fun primaryLabel(step: OnboardingStep, retry: Boolean): Int = when (step) {
    OnboardingStep.Welcome -> R.string.onboarding_continue
    OnboardingStep.DefaultHome -> R.string.onboarding_home_action
    OnboardingStep.Favorites -> R.string.onboarding_favorites_action
    OnboardingStep.NotificationAccess ->
        if (retry) R.string.onboarding_access_retry else R.string.onboarding_access_action
    OnboardingStep.Calendar -> R.string.onboarding_calendar_action
    OnboardingStep.Done -> R.string.onboarding_done_action
}

internal val OnboardingStep.title: Int
    @StringRes get() = when (this) {
        OnboardingStep.Welcome -> R.string.onboarding_welcome_title
        OnboardingStep.DefaultHome -> R.string.onboarding_home_title
        OnboardingStep.Favorites -> R.string.onboarding_favorites_title
        OnboardingStep.NotificationAccess -> R.string.onboarding_access_title
        OnboardingStep.Calendar -> R.string.onboarding_calendar_title
        OnboardingStep.Done -> R.string.onboarding_done_title
    }

internal val OnboardingStep.body: Int
    @StringRes get() = when (this) {
        OnboardingStep.Welcome -> R.string.onboarding_welcome_body
        OnboardingStep.DefaultHome -> R.string.onboarding_home_body
        OnboardingStep.Favorites -> R.string.onboarding_favorites_body
        OnboardingStep.NotificationAccess -> R.string.onboarding_access_body
        OnboardingStep.Calendar -> R.string.onboarding_calendar_body
        OnboardingStep.Done -> R.string.onboarding_done_body
    }
