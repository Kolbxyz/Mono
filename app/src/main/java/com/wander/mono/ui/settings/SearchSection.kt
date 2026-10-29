package com.wander.mono.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.wander.mono.R
import com.wander.mono.data.settings.SearchSettings

/** What search looks in besides apps and shortcuts. */
@Composable
fun SearchSection(settings: SettingsViewModel) {
    SettingsGroup(R.string.settings_search) {
        ContactsSwitch(settings)
    }
}

/**
 * Contacts in search. Switching it on asks for `READ_CONTACTS` and only turns on when granted. It
 * shows as off while the permission is missing (revoked in Android's settings, say), re-checked
 * each time the screen resumes, and search then leaves contacts out.
 */
@Composable
private fun ContactsSwitch(settings: SettingsViewModel) {
    val enabled = settings.collect(SearchSettings.Contacts) ?: return
    val context = LocalContext.current
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val granted = remember(lifecycle, enabled) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED
    }
    val request = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        settings.set(SearchSettings.Contacts, allowed)
    }
    val checked = enabled && granted
    ListItem(
        supportingContent = { Text(stringResource(R.string.settings_search_contacts_summary)) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        modifier = Modifier.toggleable(checked, role = Role.Switch) { on ->
            when {
                !on -> settings.set(SearchSettings.Contacts, false)
                granted -> settings.set(SearchSettings.Contacts, true)
                else -> request.launch(Manifest.permission.READ_CONTACTS)
            }
        },
    ) {
        Text(stringResource(R.string.settings_search_contacts))
    }
}
