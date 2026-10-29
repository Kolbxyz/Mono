package com.wander.mono.core.security

import com.wander.mono.data.settings.HiddenAppSettings
import com.wander.mono.data.settings.SettingsStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/**
 * Whether hidden apps may show in search and in settings: always without the lock; with it, only
 * after the screen lock was confirmed, until the drawer or settings close. Kept in memory only, so
 * a new process starts locked.
 */
@Singleton
class HiddenAppsLock @Inject constructor(settings: SettingsStore) {
    private val unlocked = MutableStateFlow(false)

    val locks: Flow<Boolean> = settings[HiddenAppSettings.Lock]

    val revealed: Flow<Boolean> = combine(locks, unlocked) { lock, open -> !lock || open }

    fun unlock() {
        unlocked.value = true
    }

    fun relock() {
        unlocked.value = false
    }
}
