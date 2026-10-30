package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.Routine
import com.wander.mono.data.settings.RoutineSettings

/**
 * Routines: while on, up to [Routine.MAX] daily windows, each putting the favourites picked for it
 * first on home. Off, and with none, until the user wants them.
 */
@Composable
fun RoutinesSection(settings: SettingsViewModel, routines: RoutinesViewModel = viewModel()) {
    val on = settings.collect(RoutineSettings.Enabled) ?: return
    val all by routines.routines.collectAsStateWithLifecycle()
    val favorites by routines.favorites.collectAsStateWithLifecycle()
    SettingsGroup(R.string.settings_routines) {
        SwitchSetting(
            settings,
            RoutineSettings.Enabled,
            title = R.string.settings_routines_on,
            summary = R.string.settings_routines_summary,
            icon = R.drawable.sym_schedule,
        )
        val list = all
        if (on && list != null) {
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
                ActionRow(title = R.string.settings_routine_add, icon = R.drawable.sym_add, onClick = routines::add)
            }
        }
    }
}
