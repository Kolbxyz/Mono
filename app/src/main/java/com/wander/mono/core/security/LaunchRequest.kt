package com.wander.mono.core.security

import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.LaunchSource
import com.wander.mono.core.notifications.PackageUser

/** A launch of a locked app, held back until the screen lock is confirmed, then run as asked. */
sealed interface LaunchRequest {
    /** The app the lock is about: a shortcut opens into its app, so the same lock holds it. */
    val app: PackageUser

    class App(val key: AppKey, val source: LaunchSource?) : LaunchRequest {
        override val app = PackageUser(key.packageName, key.userSerial)
    }

    class Shortcut(val shortcut: AppShortcut) : LaunchRequest {
        override val app = PackageUser(shortcut.packageName, shortcut.userSerial)
    }
}

/** Whether a lock on one of these keys holds [app]. Locks follow the package, so all its entries. */
internal fun Set<AppKey>.holds(app: PackageUser): Boolean =
    any { it.packageName == app.packageName && it.userSerial == app.userSerial }
