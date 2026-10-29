package com.wander.mono.core.apps

/**
 * An app entry as the launcher shows it. Work-profile apps have [isMainProfile] false and get a badge.
 * [detail] is set only when another entry in the same profile has the same label (two "Camera"
 * activities from different apps): it names the app, so the two rows can be told apart.
 * [systemLabel] is the app's own name when the user renamed it, so [label] is the user's name.
 */
data class LaunchableApp(
    val key: AppKey,
    val label: String,
    val isMainProfile: Boolean,
    val detail: String? = null,
    val systemLabel: String? = null,
)
