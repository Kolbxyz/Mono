package com.wander.mono.core.apps

/** A shortcut an app publishes (manifest, dynamic, pinned or cached), such as a Phalanx conversation. */
data class AppShortcut(val packageName: String, val id: String, val userSerial: Long, val label: String)
