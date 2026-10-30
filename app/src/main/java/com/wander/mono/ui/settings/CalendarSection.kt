package com.wander.mono.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.core.calendar.CalendarAccess

/**
 * Upcoming events on home. Switching it on asks for calendar access, the summary saying why;
 * after a refusal the row says so, and a further tap opens App info, where the grant still is.
 */
@Composable
fun CalendarSection(calendar: CalendarSettingsViewModel = viewModel()) {
    val initial = remember { calendar.currentlyShown() }
    val shown = calendar.shown.collectAsStateWithLifecycle(initial).value ?: return
    val granted by calendar.granted.collectAsStateWithLifecycle()
    var refused by rememberSaveable { mutableStateOf(false) }
    // Granted later in App info: the refusal no longer applies.
    val denied = refused && !granted
    val request = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        refused = !allowed
        calendar.onPermissionResult(allowed)
    }
    SettingsGroup(R.string.settings_calendar) {
        SwitchRow(
            checked = shown,
            onCheckedChange = { on ->
                when {
                    !on -> calendar.setShown(false)
                    granted -> calendar.setShown(true)
                    denied -> calendar.openAppInfo()
                    else -> request.launch(CalendarAccess.PERMISSION)
                }
            },
            title = R.string.settings_calendar_show,
            summary = if (denied) R.string.settings_calendar_denied_short else R.string.settings_calendar_short,
            icon = R.drawable.sym_event,
        )
    }
}
