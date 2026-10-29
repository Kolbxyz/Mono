package com.wander.mono.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.core.calendar.CalendarAccess
import com.wander.mono.ui.components.CheckSwitch

/**
 * Upcoming events on home. Switching it on asks for calendar access with the reason in the summary;
 * after a refusal the row says so, and a further tap opens App info, where the grant still is.
 */
@Composable
fun CalendarSection(calendar: CalendarSettingsViewModel = viewModel()) {
    val shown = calendar.shown.collectAsStateWithLifecycle(null).value ?: return
    val granted by calendar.granted.collectAsStateWithLifecycle()
    var refused by rememberSaveable { mutableStateOf(false) }
    // Granted later in App info: the refusal no longer applies.
    val denied = refused && !granted
    val request = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        refused = !allowed
        calendar.onPermissionResult(allowed)
    }
    SettingsGroup(R.string.settings_calendar) {
        ListItem(
            colors = settingRowColors(),
            supportingContent = {
                val summary = if (denied) R.string.settings_calendar_denied else R.string.settings_calendar_summary
                Text(stringResource(summary))
            },
            trailingContent = { CheckSwitch(shown) },
            modifier = Modifier.toggleable(shown, role = Role.Switch) { on ->
                when {
                    !on -> calendar.setShown(false)
                    granted -> calendar.setShown(true)
                    denied -> calendar.openAppInfo()
                    else -> request.launch(CalendarAccess.PERMISSION)
                }
            },
        ) {
            Text(stringResource(R.string.settings_calendar_show))
        }
    }
}
