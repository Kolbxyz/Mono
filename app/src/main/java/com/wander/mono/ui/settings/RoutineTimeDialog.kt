package com.wander.mono.ui.settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.wander.mono.R
import com.wander.mono.data.Routine
import android.text.format.DateFormat as AndroidDateFormat

/**
 * Sets one end of a routine's window: Material's time entry (typed hours and minutes, which fits a
 * dialog where the dial would not), in the user's 12- or 24-hour style, starting on [minute].
 */
@Composable
internal fun RoutineTimeDialog(minute: Int, onSet: (Int) -> Unit, onDismiss: () -> Unit) {
    val state = rememberTimePickerState(
        initialHour = minute / Routine.MINUTES_PER_HOUR,
        initialMinute = minute % Routine.MINUTES_PER_HOUR,
        is24Hour = AndroidDateFormat.is24HourFormat(LocalContext.current),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(painterResource(R.drawable.sym_schedule), contentDescription = null) },
        title = { Text(stringResource(R.string.settings_routine_time_title)) },
        text = { TimeInput(state) },
        confirmButton = {
            TextButton(onClick = { onSet(state.hour * Routine.MINUTES_PER_HOUR + state.minute) }) {
                Text(stringResource(R.string.action_set))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.rename_cancel)) } },
    )
}
