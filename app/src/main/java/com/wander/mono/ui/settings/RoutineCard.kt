package com.wander.mono.ui.settings

import android.icu.text.DateFormat
import android.icu.util.TimeZone
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.data.Routine
import java.util.Date
import android.text.format.DateFormat as AndroidDateFormat

/**
 * One routine, as one strip of the routines group: its number, its window as two time buttons
 * (each opening a time entry), a remove button, then the favourites as chips to pick for it.
 */
@Composable
internal fun RoutineCard(
    routine: Routine,
    number: Int,
    favorites: List<LaunchableApp>,
    onChange: (Routine) -> Unit,
    onRemove: () -> Unit,
) {
    // Which end of the window is being set, if any; kept through rotation.
    var editing by rememberSaveable { mutableStateOf<RoutineEnd?>(null) }
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.settings_routine_title, number),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRemove) {
                Icon(
                    painterResource(R.drawable.sym_delete),
                    contentDescription = stringResource(R.string.settings_routine_remove, number),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TimeButton(routine.start, R.string.settings_routine_from) { editing = RoutineEnd.Start }
            Text(stringResource(R.string.settings_routine_to), color = MaterialTheme.colorScheme.onSurfaceVariant)
            TimeButton(routine.end, R.string.settings_routine_until) { editing = RoutineEnd.End }
        }
        if (favorites.isEmpty()) {
            Text(
                stringResource(R.string.settings_routine_no_favorites),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        } else {
            FlowRow(
                Modifier.padding(top = 12.dp, end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                favorites.forEach { app ->
                    val picked = app.key in routine.apps
                    FilterChip(
                        selected = picked,
                        onClick = { onChange(routine.copy(apps = routine.apps.toggled(app.key, !picked))) },
                        label = { Text(app.label) },
                    )
                }
            }
        }
    }
    editing?.let { end ->
        RoutineTimeDialog(
            minute = if (end == RoutineEnd.Start) routine.start else routine.end,
            onSet = { minute ->
                editing = null
                onChange(if (end == RoutineEnd.Start) routine.copy(start = minute) else routine.copy(end = minute))
            },
            onDismiss = { editing = null },
        )
    }
}

/** A window end as a tonal button showing its time; TalkBack hears what it sets. */
@Composable
private fun TimeButton(minute: Int, @StringRes description: Int, onClick: () -> Unit) {
    val time = timeOfDay(minute)
    val label = stringResource(description, time)
    FilledTonalButton(onClick = onClick, modifier = Modifier.semantics { contentDescription = label }) {
        Text(time, style = MaterialTheme.typography.titleMedium)
    }
}

/** [minute] after midnight in the user's 12- or 24-hour style. */
@Composable
private fun timeOfDay(minute: Int): String {
    val locale = LocalConfiguration.current.locales[0]
    val skeleton = if (AndroidDateFormat.is24HourFormat(LocalContext.current)) "Hm" else "hm"
    return remember(minute, skeleton, locale) {
        DateFormat.getInstanceForSkeleton(skeleton, locale)
            .apply { timeZone = TimeZone.GMT_ZONE }
            .format(Date(minute * MILLIS_PER_MINUTE))
    }
}

private fun <T> Set<T>.toggled(item: T, on: Boolean): Set<T> = if (on) this + item else this - item

private enum class RoutineEnd { Start, End }

private const val MILLIS_PER_MINUTE = 60_000L
