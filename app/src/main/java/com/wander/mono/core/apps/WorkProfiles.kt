package com.wander.mono.core.apps

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import android.os.UserManager
import com.wander.mono.R
import com.wander.mono.di.AppScope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * A work (or other non-private) profile, listed apart at the bottom of the drawer. [paused] is
 * Android's quiet mode: its apps cannot run. [canToggle] is false before Android 9, where a launcher
 * cannot pause or resume a profile.
 */
data class WorkProfile(val serial: Long, val paused: Boolean, val canToggle: Boolean)

/**
 * The work profiles and whether each is paused, re-read on each profile event ([CatalogChange.Profiles]),
 * never polled; pausing and resuming them. The private space is [PrivateSpace]'s.
 */
@Singleton
class WorkProfiles @Inject constructor(
    @ApplicationContext context: Context,
    private val profiles: Profiles,
    events: CatalogEvents,
    @AppScope scope: CoroutineScope,
) {
    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val userManager = context.getSystemService(UserManager::class.java)

    val all: StateFlow<List<WorkProfile>> = events.changes
        .filter { it is CatalogChange.Profiles }
        .onStart { emit(CatalogChange.Profiles) }
        .conflate()
        .map { read() }
        .flowOn(Dispatchers.IO)
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** Resuming may first show the profile's own lock, from the system; the broadcast after updates [all]. */
    fun setPaused(profile: WorkProfile, paused: Boolean): ActionResult {
        val user = profiles.userOf(profile.serial)
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.P -> ActionResult.Failed(R.string.error_not_allowed)
            user == null -> ActionResult.Failed(R.string.error_profile_gone)
            else -> try {
                // False only means Android is asking for the profile's credential first.
                userManager.requestQuietModeEnabled(paused, user)
                ActionResult.Done
            } catch (_: SecurityException) {
                // Only the default home app in front may ask.
                ActionResult.Failed(R.string.error_not_allowed)
            }
        }
    }

    private fun read(): List<WorkProfile> = launcherApps.profiles
        .filter { it != profiles.main && !profiles.isPrivate(it) }
        .map { user ->
            WorkProfile(
                serial = profiles.serialOf(user),
                paused = profiles.isLocked(user),
                canToggle = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P,
            )
        }
}
