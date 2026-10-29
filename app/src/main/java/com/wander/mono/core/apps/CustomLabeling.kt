package com.wander.mono.core.apps

/**
 * [apps] with the user's names from [labels] applied. A renamed app keeps its own name in
 * [LaunchableApp.systemLabel] and drops the clash [LaunchableApp.detail]: the user's name is the
 * one that tells it apart now.
 */
internal fun withCustomLabels(apps: List<LaunchableApp>, labels: Map<AppKey, String>): List<LaunchableApp> {
    if (labels.isEmpty()) return apps
    return apps.map { app ->
        val custom = labels[app.key]
        if (custom == null || custom == app.label) app else app.copy(label = custom, detail = null, systemLabel = app.label)
    }
}
