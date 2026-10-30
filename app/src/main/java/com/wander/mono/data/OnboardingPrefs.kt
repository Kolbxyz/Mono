package com.wander.mono.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.onboardingStore by preferencesDataStore(name = "onboarding")

/**
 * Whether first-run setup was finished or skipped, and whether the drawer has been opened since;
 * a file of its own, apart from favourites and settings.
 */
@Singleton
class OnboardingPrefs @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.onboardingStore

    val done: Flow<Boolean> = store.data
        .map { it[DONE] == true }
        .distinctUntilChanged()

    /** Setup is over and the drawer was never opened: home still teaches the swipe up. */
    val showsSwipeHint: Flow<Boolean> = store.data
        .map { it[DONE] == true && it[DRAWER_OPENED] != true }
        .distinctUntilChanged()

    suspend fun markDone() {
        store.edit { it[DONE] = true }
    }

    suspend fun markDrawerOpened() {
        store.edit { it[DRAWER_OPENED] = true }
    }

    private companion object {
        val DONE = booleanPreferencesKey("done")
        val DRAWER_OPENED = booleanPreferencesKey("drawer_opened")
    }
}
