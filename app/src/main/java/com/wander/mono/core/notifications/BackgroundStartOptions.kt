package com.wander.mono.core.notifications

import android.app.ActivityOptions
import android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE
import android.os.Build
import android.os.Bundle

/**
 * Android 14+ only lets a sent intent start an activity from the background with the
 * sender's consent; home is in front when the user taps, so it lends its own.
 */
internal fun backgroundStartOptions(): Bundle? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA -> ActivityOptions.makeBasic()
        .setPendingIntentBackgroundActivityStartMode(MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE)
        .toBundle()
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> ActivityOptions.makeBasic()
        .setPendingIntentBackgroundActivityStartMode(legacyAllowedMode())
        .toBundle()
    else -> null
}

@Suppress("DEPRECATION") // Replaced by ALLOW_IF_VISIBLE, which only exists from Android 16.
private fun legacyAllowedMode(): Int = ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
