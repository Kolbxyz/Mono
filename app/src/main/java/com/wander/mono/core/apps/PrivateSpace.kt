package com.wander.mono.core.apps

import android.app.ActivityOptions
import android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.LauncherApps
import android.os.Build
import android.os.UserManager
import androidx.annotation.RequiresApi
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

/** Android 15's private space as the drawer shows it. */
sealed interface PrivateSpaceState {
    /** No private space, Mono may not see it, or it is locked and the user asked for no trace of it then. */
    data object Absent : PrivateSpaceState

    /** [canOpenSettings]: Android 16 lets the launcher open the private space's own settings. */
    data class Present(val locked: Boolean, val canOpenSettings: Boolean) : PrivateSpaceState
}

/**
 * Whether the private space shows and is locked, re-read on each profile event and checked again
 * each time Mono comes to the front; locking and unlocking it.
 */
@Singleton
class PrivateSpace @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profiles: Profiles,
    private val events: CatalogEvents,
    @AppScope scope: CoroutineScope,
) {
    private val userManager = context.getSystemService(UserManager::class.java)
    private val launcherApps = context.getSystemService(LauncherApps::class.java)

    val state: StateFlow<PrivateSpaceState> = events.changes
        .filter { it is CatalogChange.Profiles }
        .onStart { emit(CatalogChange.Profiles) }
        .conflate()
        .map { read() }
        .flowOn(Dispatchers.IO)
        .stateIn(scope, SharingStarted.Eagerly, PrivateSpaceState.Absent)

    private fun read(): PrivateSpaceState = profiles.privateSpace()?.let { user ->
        // Locked until its apps can be listed, so it never shows open with none of them.
        val locked = !profiles.isReady(user)
        if (locked && profiles.hidesWhileLocked(user)) {
            null
        } else {
            PrivateSpaceState.Present(locked, canOpenSettings = Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA)
        }
    } ?: PrivateSpaceState.Absent

    /**
     * Back in front: when the private space is no longer as [state] last read it (it became ready with
     * no event to say so), every profile listener re-reads it, the catalog included. Nothing otherwise.
     */
    fun recheck() {
        if (read() != state.value) events.recheckProfiles()
    }

    /**
     * Unlocking asks for the private space's own lock, shown by the system; the profile broadcast
     * that follows updates [state]. Only the default home app in front may ask.
     */
    fun setLocked(locked: Boolean): ActionResult {
        val user = profiles.privateSpace()
        if (user == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return ActionResult.Failed(R.string.error_profile_gone)
        }
        return try {
            userManager.requestQuietModeEnabled(locked, user)
            ActionResult.Done
        } catch (_: SecurityException) {
            ActionResult.Failed(R.string.error_not_allowed)
        }
    }

    /** Android 16 only: Android 15 gives launchers no way into the private space's settings. */
    fun openSettings(): ActionResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        startSettings()
    } else {
        ActionResult.Failed(R.string.error_app_missing)
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    private fun startSettings(): ActionResult {
        val sender = launcherApps.privateSpaceSettingsIntent ?: return ActionResult.Failed(R.string.error_app_missing)
        // The sender is the system's; Mono is in front, so its own right to start an activity applies.
        val options = ActivityOptions.makeBasic()
            .setPendingIntentBackgroundActivityStartMode(MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE)
            .toBundle()
        return try {
            val newTask = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startIntentSender(sender, null, newTask, newTask, 0, options)
            ActionResult.Done
        } catch (_: IntentSender.SendIntentException) {
            ActionResult.Failed(R.string.error_app_missing)
        }
    }
}
