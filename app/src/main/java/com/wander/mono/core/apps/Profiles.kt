package com.wander.mono.core.apps

import android.content.Context
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

    val main: UserHandle = Process.myUserHandle()

    fun serialOf(user: UserHandle): Long = userManager.getSerialNumberForUser(user)

    /** Null once the profile has been removed. */
    fun userOf(serial: Long): UserHandle? = userManager.getUserForSerialNumber(serial)
}
