package com.wander.mono.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wander.mono.core.apps.AppKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.swipeStore by preferencesDataStore(name = "swipes")

/**
 * What swiping each favourite left does, chosen from its options. Nothing by default, so only the
 * choices are stored; they are kept when the app leaves home, like its folder.
 */
@Singleton
class FavoriteSwipes @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.swipeStore

    val actions: Flow<Map<AppKey, SwipeAction>> = store.data
        .map { it.readActions() }
        .distinctUntilChanged()

    /** A null [action] makes the swipe do nothing again. */
    suspend fun set(app: AppKey, action: SwipeAction?) {
        store.edit { prefs ->
            val others = prefs.readActions() - app
            prefs[ACTIONS] = (if (action == null) others else others + (app to action)).encodeAll()
        }
    }

    /** Replaces every choice, for a restored backup. */
    suspend fun replace(actions: Map<AppKey, SwipeAction>) {
        store.edit { prefs -> prefs[ACTIONS] = actions.encodeAll() }
    }

    private fun Preferences.readActions(): Map<AppKey, SwipeAction> =
        this[ACTIONS].orEmpty().mapNotNull(SwipeEntry::decode).toMap()

    private fun Map<AppKey, SwipeAction>.encodeAll(): Set<String> =
        entries.mapTo(mutableSetOf()) { (app, action) -> SwipeEntry.encode(app, action) }

    private companion object {
        val ACTIONS = stringSetPreferencesKey("swipe_left")
    }
}
