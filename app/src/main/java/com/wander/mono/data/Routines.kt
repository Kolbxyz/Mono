package com.wander.mono.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.routineStore by preferencesDataStore(name = "routines")

/**
 * The user's routines, in the order they were added: a few lines in a file of their own, so they
 * never touch the saved home order. None until the user adds one.
 */
@Singleton
class Routines @Inject constructor(@ApplicationContext context: Context) {
    private val store = context.routineStore

    val routines: Flow<List<Routine>> = store.data
        .map { it.readRoutines() }
        .distinctUntilChanged()

    /** Adds [routine] unless there are already [Routine.MAX]. */
    suspend fun add(routine: Routine) = edit { if (it.size < Routine.MAX) it + routine else it }

    /** Replaces the routine at [index], as the settings list shows them. */
    suspend fun update(index: Int, routine: Routine) =
        edit { routines -> routines.mapIndexed { i, old -> if (i == index) routine else old } }

    suspend fun remove(index: Int) = edit { routines -> routines.filterIndexed { i, _ -> i != index } }

    /** Replaces every routine, for a restored backup. */
    suspend fun replace(routines: List<Routine>) {
        store.edit { it.writeRoutines(routines.take(Routine.MAX)) }
    }

    private suspend fun edit(change: (List<Routine>) -> List<Routine>) {
        store.edit { prefs -> prefs.writeRoutines(change(prefs.readRoutines())) }
    }

    private fun Preferences.readRoutines(): List<Routine> =
        this[ROUTINES].orEmpty().lines().mapNotNull(Routine::decode).take(Routine.MAX)

    private fun MutablePreferences.writeRoutines(routines: List<Routine>) {
        this[ROUTINES] = routines.joinToString("\n") { it.encode() }
    }

    private companion object {
        val ROUTINES = stringPreferencesKey("routines")
    }
}
