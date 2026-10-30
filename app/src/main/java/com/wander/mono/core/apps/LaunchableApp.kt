package com.wander.mono.core.apps

/**
 * An app entry as the launcher shows it. Work-profile apps have [isMainProfile] false and get a badge;
 * private space apps have [isPrivate] too, are listed apart in the drawer and never go on home.
 * [detail] is set only when another entry in the same profile has the same label (two "Camera"
 * activities from different apps): it names the app, so the two rows can be told apart.
 * [systemLabel] is the app's own name when the user renamed it, so [label] is the user's name.
 * [category] is what the app declares itself to be; [installedAt] when it was first installed (main
 * profile only, null elsewhere); [archived] (Android 15+) that its code was offloaded and launching
 * it has the system restore it first.
 */
data class LaunchableApp(
    val key: AppKey,
    val label: String,
    val isMainProfile: Boolean,
    val detail: String? = null,
    val systemLabel: String? = null,
    val isPrivate: Boolean = false,
    val category: AppCategory = AppCategory.Other,
    val installedAt: Long? = null,
    val archived: Boolean = false,
)
