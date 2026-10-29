package com.wander.mono.core.notifications

/** An app in one profile, which is how notifications are counted. */
data class PackageUser(val packageName: String, val userSerial: Long)
