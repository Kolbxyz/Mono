package com.wander.mono.core.apps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.content.pm.LauncherUserInfo
import android.content.pm.ShortcutInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import androidx.core.content.ContextCompat
import com.wander.mono.di.AppScope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

/**
 * The single `LauncherApps.Callback` and profile receiver of the process. Registered for as long as
 * the process lives: the system only calls it when something changes, so it costs nothing at rest,
 * and the catalog never goes stale while the launcher is in the background.
 */
@Singleton
class CatalogEvents @Inject constructor(
    @ApplicationContext context: Context,
    @AppScope scope: CoroutineScope,
) {
    private val launcherApps = context.getSystemService(LauncherApps::class.java)

    val changes: SharedFlow<CatalogChange> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                trySend(CatalogChange.Packages(setOf(packageName)))
            }

            override fun onPackageAdded(packageName: String, user: UserHandle) {
                trySend(CatalogChange.Packages(setOf(packageName)))
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {
                trySend(CatalogChange.Packages(setOf(packageName)))
            }

            override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                trySend(CatalogChange.Packages(packageNames.toSet()))
            }

            override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                trySend(CatalogChange.Packages(packageNames.toSet()))
            }

            override fun onShortcutsChanged(packageName: String, shortcuts: List<ShortcutInfo>, user: UserHandle) {
                trySend(CatalogChange.Shortcuts(packageName))
            }

            // Android 16: the user changed whether a locked private space leaves any trace.
            override fun onUserConfigChanged(launcherUserInfo: LauncherUserInfo) {
                trySend(CatalogChange.Profiles)
            }
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(CatalogChange.Profiles)
            }
        }
        val profileActions = IntentFilter().apply {
            addAction(Intent.ACTION_MANAGED_PROFILE_ADDED)
            addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED)
            addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
            addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
            addAction(Intent.ACTION_MANAGED_PROFILE_UNLOCKED)
            // The private space (Android 15+): the managed ones above only cover work profiles. Unlocking
            // makes it available, then accessible once its apps can be listed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                addAction(Intent.ACTION_PROFILE_ADDED)
                addAction(Intent.ACTION_PROFILE_REMOVED)
                addAction(Intent.ACTION_PROFILE_ACCESSIBLE)
                addAction(Intent.ACTION_PROFILE_INACCESSIBLE)
                addAction(Intent.ACTION_PROFILE_AVAILABLE)
                addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
            }
        }
        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        ContextCompat.registerReceiver(context, receiver, profileActions, ContextCompat.RECEIVER_NOT_EXPORTED)
        awaitClose {
            launcherApps.unregisterCallback(callback)
            context.unregisterReceiver(receiver)
        }
    }.shareIn(scope, SharingStarted.Eagerly)
}
