package com.wander.mono.core.apps

/** A catalog entry with the name of the app that owns the activity, before clashes are resolved. */
internal data class LabeledActivity(val app: LaunchableApp, val appName: String)

/**
 * Gives every entry whose label is shared by another entry of the same profile a [LaunchableApp.detail]:
 * the owning app's name when that tells it apart from the label and from the others, else the
 * package name, which always does. Entries with a unique label are left untouched.
 */
internal fun resolveLabelClashes(entries: List<LabeledActivity>): List<LaunchableApp> {
    val groups = entries.groupBy { it.app.label.lowercase() to it.app.key.userSerial }
    return entries.map { entry ->
        val group = groups.getValue(entry.app.label.lowercase() to entry.app.key.userSerial)
        if (group.size == 1) return@map entry.app
        val names = group.map { it.appName.lowercase() }
        val nameHelps = !entry.appName.equals(entry.app.label, ignoreCase = true) &&
            names.count { it == entry.appName.lowercase() } == 1
        entry.app.copy(detail = if (nameHelps) entry.appName else entry.app.key.packageName)
    }
}
