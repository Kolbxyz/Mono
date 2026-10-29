package com.wander.mono.core.apps

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Translates between `UserHandle`s and the persisted serial numbers in [AppKey]. */
@Singleton
class Profiles @Inject constructor(@ApplicationContext context: Context) {
    private val userManager = context.getSystemService(UserManager::class.java)
    private val launcherApps = context.getSystemService(LauncherApps::class.java)

    val main: UserHandle = Process.myUserHandle()

    fun serialOf(user: UserHandle): Long = userManager.getSerialNumberForUser(user)

    /** Serials of the other profiles this launcher can show (work profile, private space). */
    fun otherSerials(): Set<Long> = launcherApps.profiles.filter { it != main }.mapTo(mutableSetOf(), ::serialOf)

    /** Null once the profile has been removed. */
    fun userOf(serial: Long): UserHandle? = userManager.getUserForSerialNumber(serial)
}
