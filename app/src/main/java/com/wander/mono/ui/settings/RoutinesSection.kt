package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.Routine

/**
 * Routines: up to [Routine.MAX] daily windows, each putting the favourites picked for it first on
 * home. None until the user adds one; with none, nothing changes on home and nothing is watched.
 */
@Composable
fun RoutinesSection(routines: RoutinesViewModel = viewModel()) {
    val list = routines.routines.collectAsStateWithLifecycle().value ?: return
    val favorites by routines.favorites.collectAsStateWithLifecycle()
    SettingsGroup(R.string.settings_routines) {
        list.forEachIndexed { index, routine ->
            RoutineCard(
                routine = routine,
                number = index + 1,
                favorites = favorites,
                onChange = { routines.update(index, it) },
                onRemove = { routines.remove(index) },
            )
        }
        if (list.size < Routine.MAX) {
            ActionRow(
                title = R.string.settings_routine_add,
                // Until there is one, the row says what a routine is.
                summary = R.string.settings_routines_summary.takeIf { list.isEmpty() },
                icon = R.drawable.sym_add,
                onClick = routines::add,
            )
        }
    }
}
