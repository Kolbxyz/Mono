package com.wander.mono.core.security

import android.app.PendingIntent
import android.content.Intent
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.core.apps.LaunchSource
import com.wander.mono.core.notifications.PackageUser

/** A launch of a locked app (or its notification), held back until the screen lock is confirmed, then run as asked. */
sealed interface LaunchRequest {
    /** The app the lock is about: a shortcut opens into its app, so the same lock holds it. */
    val app: PackageUser

    class App(val key: AppKey, val source: LaunchSource?) : LaunchRequest {
        override val app = PackageUser(key.packageName, key.userSerial)
    }

    class Shortcut(val shortcut: AppShortcut) : LaunchRequest {
        override val app = PackageUser(shortcut.packageName, shortcut.userSerial)
    }

    /**
     * One of [app]'s notification intents: its tap target, a button or a reply ([fillIn] carries
     * the typed text). Once sent, the notification [cancels] names is cleared, as auto-cancel asks.
     */
    class Notification(
        override val app: PackageUser,
        val intent: PendingIntent,
        val fillIn: Intent? = null,
        val cancels: String? = null,
    ) : LaunchRequest
}

/** Whether a lock on one of these keys holds [app]. Locks follow the package, so all its entries. */
internal fun Set<AppKey>.holds(app: PackageUser): Boolean =
    any { it.packageName == app.packageName && it.userSerial == app.userSerial }
