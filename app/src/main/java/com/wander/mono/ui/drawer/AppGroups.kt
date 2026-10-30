package com.wander.mono.ui.drawer

import java.util.concurrent.TimeUnit

/** What heads a group of the drawer's list. */
sealed interface GroupHead {
    data class Letter(val letter: String) : GroupHead

    /** Apps installed in the last few days, at the top of the A–Z list, also under their letters. */
    data object New : GroupHead

    data class Age(val age: InstallAge) : GroupHead
}

/** How long ago an app was first installed, for the list sorted newest first. */
enum class InstallAge { Day, Week, Month, Earlier }

/** One item of the list: a heading and the apps under it. */
data class AppGroup(val head: GroupHead, val entries: List<DrawerEntry>) {
    val key: String
        get() = when (head) {
            is GroupHead.Letter -> "letter:${head.letter}"
            GroupHead.New -> "new"
            is GroupHead.Age -> "age:${head.age}"
        }
}

/**
 * The list's groups, the letter rail's sections, and [lead]: how many groups come before the first
 * letter, so rail entries and list items can be matched.
 */
internal data class ListShape(val groups: List<AppGroup>, val sections: List<Section>, val lead: Int)

/** Letter groups, after a "New" group of the [apps] marked fresh when [withNew] and there are any. */
internal fun alphabetical(apps: List<DrawerEntry>, withNew: Boolean): ListShape {
    val sections = sectionsOf(apps.map { it.app.label })
    val letters = sections.mapIndexed { i, section ->
        val end = sections.getOrNull(i + 1)?.index ?: apps.size
        AppGroup(GroupHead.Letter(section.letter), apps.subList(section.index, end))
    }
    val fresh = apps.filter { it.fresh }
    return if (withNew && fresh.isNotEmpty()) {
        ListShape(listOf(AppGroup(GroupHead.New, fresh)) + letters, sections, lead = 1)
    } else {
        ListShape(letters, sections, lead = 0)
    }
}

/**
 * Newest install first, grouped by how long ago; apps with no install time (other profiles) last,
 * in name order. No letters, so no rail.
 */
internal fun byInstallAge(apps: List<DrawerEntry>, now: Long): ListShape {
    val sorted = apps.sortedByDescending { it.app.installedAt ?: Long.MIN_VALUE }
    val groups = sorted.groupBy { installAge(it.app.installedAt, now) }
        .map { (age, entries) -> AppGroup(GroupHead.Age(age), entries) }
    return ListShape(groups, sections = emptyList(), lead = 0)
}

/** Which [InstallAge] an app first installed at [installedAt] falls in at [now]. */
internal fun installAge(installedAt: Long?, now: Long): InstallAge {
    val elapsed = installedAt?.let { now - it } ?: return InstallAge.Earlier
    return when {
        elapsed < TimeUnit.DAYS.toMillis(1) -> InstallAge.Day
        elapsed < TimeUnit.DAYS.toMillis(WEEK_DAYS) -> InstallAge.Week
        elapsed < TimeUnit.DAYS.toMillis(MONTH_DAYS) -> InstallAge.Month
        else -> InstallAge.Earlier
    }
}

private const val WEEK_DAYS = 7L
private const val MONTH_DAYS = 30L
