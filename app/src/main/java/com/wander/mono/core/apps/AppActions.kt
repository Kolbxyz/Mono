package com.wander.mono.core.apps

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.UserHandle
import com.wander.mono.MainActivity
import com.wander.mono.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Launch, app info, uninstall and shortcuts, all through `LauncherApps` so other profiles work too. */
@Singleton
class AppActions @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profiles: Profiles,
) {
    private val launcherApps = context.getSystemService(LauncherApps::class.java)

    fun launch(key: AppKey): ActionResult = inProfile(key.userSerial) { user ->
        launcherApps.startMainActivity(key.component(), user, null, null)
    }

    fun openAppInfo(key: AppKey): ActionResult = inProfile(key.userSerial) { user ->
        launcherApps.startAppDetailsActivity(key.component(), user, null, null)
    }

    /** Mono's own App info page, where Android 13+ keeps "Allow restricted settings". */
    fun openOwnAppInfo(): ActionResult = inProfile(profiles.serialOf(profiles.main)) { user ->
        launcherApps.startAppDetailsActivity(ComponentName(context, MainActivity::class.java), user, null, null)
    }

    /** The system shows its own confirmation; nothing is removed from here. */
    fun uninstall(key: AppKey): ActionResult = inProfile(key.userSerial) { user ->
        val intent = Intent(Intent.ACTION_DELETE, Uri.fromParts("package", key.packageName, null))
            .putExtra(Intent.EXTRA_USER, user)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun launchShortcut(shortcut: AppShortcut): ActionResult = inProfile(shortcut.userSerial) { user ->
        launcherApps.startShortcut(shortcut.packageName, shortcut.id, null, null, user)
    }

    private inline fun inProfile(serial: Long, start: (UserHandle) -> Unit): ActionResult {
        val user = profiles.userOf(serial) ?: return ActionResult.Failed(R.string.error_profile_gone)
        return try {
            start(user)
            ActionResult.Done
        } catch (_: ActivityNotFoundException) {
            ActionResult.Failed(R.string.error_app_missing)
        } catch (_: SecurityException) {
            ActionResult.Failed(R.string.error_not_allowed)
        } catch (_: IllegalStateException) {
            // LauncherApps throws this while the profile is paused or still locked.
            ActionResult.Failed(R.string.error_profile_locked)
        }
    }

    private fun AppKey.component() = ComponentName(packageName, className)
}
