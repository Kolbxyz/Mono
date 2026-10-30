package com.wander.mono.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.core.calendar.CalendarEvent

/**
 * Join (the event's meeting link) and Map (its location), as at most two tonal chips under the
 * event's line; nothing at all when it has neither. Map shows only when an app can show a place.
 */
@Composable
internal fun CalendarEventActions(
    event: CalendarEvent,
    canOpenMaps: Boolean,
    actions: CalendarEventViewModel = viewModel(),
) {
    val join = event.meetingUrl != null
    val map = canOpenMaps && event.location != null
    if (!join && !map) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = HomeCardInset, end = HomeCardInset, bottom = 4.dp),
    ) {
        if (join) TonalAction(R.string.calendar_action_join, R.drawable.sym_arrow_outward) { actions.join(event) }
        if (map) TonalAction(R.string.calendar_action_map, R.drawable.sym_navigation) { actions.openMap(event) }
    }
}

@Composable
private fun TonalAction(@StringRes label: Int, @DrawableRes icon: Int, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    AssistChip(
        onClick = onClick,
        label = { Text(stringResource(label)) },
        leadingIcon = { Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(18.dp)) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = scheme.secondaryContainer,
            labelColor = scheme.onSecondaryContainer,
            leadingIconContentColor = scheme.onSecondaryContainer,
        ),
        border = null,
    )
}
