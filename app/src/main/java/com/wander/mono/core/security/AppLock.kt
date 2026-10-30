package com.wander.mono.core.security

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.di.AppScope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * App lock: a locked app (and its shortcuts) opens only after the screen lock. Once confirmed it
 * stays open until the screen goes off; that is kept in memory only, so a new process starts with
 * every locked app locked. [AppActions][com.wander.mono.core.apps.AppActions] hands each held
 * launch to [requests], where home asks for the screen lock and runs it.
 */
@Singleton
class AppLock @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lockedApps: LockedApps,
    @AppScope scope: CoroutineScope,
) {
    /** Null until the file is read. */
    private val locked: StateFlow<Set<AppKey>?> = lockedApps.apps.stateIn(scope, SharingStarted.Eagerly, null)
    private val unlocked = MutableStateFlow<Set<PackageUser>>(emptySet())
    private val held = Channel<LaunchRequest>(Channel.CONFLATED)

    val apps: Flow<Set<AppKey>> = lockedApps.apps

    /** Launches waiting on the screen lock, the newest only. */
    val requests: Flow<LaunchRequest> = held.receiveAsFlow()

    init {
        // The screen-off receiver is registered only while something is unlocked.
        scope.launch {
            unlocked.map { it.isNotEmpty() }.distinctUntilChanged().collectLatest { anyUnlocked ->
                if (anyUnlocked) {
                    screenOff().first()
                    unlocked.value = emptySet()
                }
            }
        }
    }

    /**
     * True when [request] must wait for the screen lock: it is then sent to [requests] and the
     * caller starts nothing. Before the locked set is read, every launch waits; [mustConfirm]
     * lets through the ones that turn out not locked.
     */
    fun holdsBack(request: LaunchRequest): Boolean {
        val keys = locked.value
        val holds = keys == null || (keys.holds(request.app) && request.app !in unlocked.value)
        if (holds) held.trySend(request)
        return holds
    }

    suspend fun mustConfirm(request: LaunchRequest): Boolean =
        locked.filterNotNull().first().holds(request.app) && request.app !in unlocked.value

    /** The screen lock was confirmed for [request]'s app: it opens freely until the screen goes off. */
    fun unlock(request: LaunchRequest) {
        unlocked.update { it + request.app }
    }

    /** Locking needs a screen lock to ask for later; without one it is refused. */
    suspend fun setLocked(key: AppKey, lock: Boolean): ActionResult {
        if (lock && !context.getSystemService(KeyguardManager::class.java).isDeviceSecure) {
            return ActionResult.Failed(R.string.error_no_screen_lock)
        }
        lockedApps.setLocked(key, lock)
        // Locked again at once, not at the next screen off.
        if (lock) unlocked.update { it - PackageUser(key.packageName, key.userSerial) }
        return ActionResult.Done
    }

    private fun screenOff(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(Unit)
            }
        }
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        awaitClose { context.unregisterReceiver(receiver) }
    }
}
