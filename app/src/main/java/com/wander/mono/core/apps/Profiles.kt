package com.wander.mono.core.apps

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.LauncherUserInfo
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Translates between `UserHandle`s and the persisted serial numbers in [AppKey], and tells the
 * profiles apart: the main one, work profiles, and Android 15's private space. Android only shows
 * the private space to the default home app holding `ACCESS_HIDDEN_PROFILES`; to anyone else
 * `LauncherApps.getProfiles()` leaves it out, so nothing here can leak it.
 */
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

    /**
     * The profiles whose apps are listed: every one, except the private space until it is ready
     * ([isReady]); before then Android lists none of its apps.
     */
    fun listed(): List<UserHandle> = launcherApps.profiles.filterNot { isPrivate(it) && !isReady(it) }

    /** The private space, when there is one and Mono may see it. */
    fun privateSpace(): UserHandle? = launcherApps.profiles.firstOrNull(::isPrivate)

    fun isPrivate(user: UserHandle): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && user != main &&
            launcherApps.getLauncherUserInfo(user)?.userType == UserManager.USER_TYPE_PROFILE_PRIVATE

    /** Locked is quiet mode: the profile's apps are stopped until the user unlocks it again. */
    fun isLocked(user: UserHandle): Boolean = userManager.isQuietModeEnabled(user)

    /**
     * Out of quiet mode and with its storage unlocked: only then are its apps listed. Android says a
     * profile is available as soon as quiet mode ends, which can be before its storage is unlocked.
     */
    fun isReady(user: UserHandle): Boolean = !isLocked(user) && userManager.isUserUnlocked(user)

    /**
     * The user chose, in the private space settings, to leave no trace of it while it is locked.
     * Android 16 hands launchers that choice; Android 15 keeps it to the system launcher.
     */
    fun hidesWhileLocked(user: UserHandle): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA &&
            launcherApps.getLauncherUserInfo(user)?.userConfig
                ?.getBoolean(LauncherUserInfo.PRIVATE_SPACE_ENTRYPOINT_HIDDEN, false) == true
}
