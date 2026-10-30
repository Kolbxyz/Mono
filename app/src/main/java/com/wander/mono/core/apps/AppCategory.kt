package com.wander.mono.core.apps

import android.content.pm.ApplicationInfo

/**
 * What an app says it is (`ApplicationInfo.category`), as the drawer's category chips group it.
 * Declaration order is the order the chips show in; [Other] is every app that declares nothing.
 */
enum class AppCategory {
    Games,
    Audio,
    Video,
    Image,
    Social,
    News,
    Maps,
    Productivity,
    Accessibility,
    Other,
}

/** The [AppCategory] of an `ApplicationInfo.category` value; undefined and unknown values are [AppCategory.Other]. */
fun appCategory(category: Int): AppCategory = when (category) {
    ApplicationInfo.CATEGORY_GAME -> AppCategory.Games
    ApplicationInfo.CATEGORY_AUDIO -> AppCategory.Audio
    ApplicationInfo.CATEGORY_VIDEO -> AppCategory.Video
    ApplicationInfo.CATEGORY_IMAGE -> AppCategory.Image
    ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.Social
    ApplicationInfo.CATEGORY_NEWS -> AppCategory.News
    ApplicationInfo.CATEGORY_MAPS -> AppCategory.Maps
    ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.Productivity
    // A plain number: Android 12 named it, but an app may declare it on any version.
    CATEGORY_ACCESSIBILITY -> AppCategory.Accessibility
    else -> AppCategory.Other
}

/** `ApplicationInfo.CATEGORY_ACCESSIBILITY`, which only Android 12's SDK names. */
private const val CATEGORY_ACCESSIBILITY = 8
