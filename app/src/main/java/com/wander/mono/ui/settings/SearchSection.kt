package com.wander.mono.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
    SwitchRow(
        checked = enabled && granted,
        onCheckedChange = { on ->
            when {
                !on -> settings.set(SearchSettings.Contacts, false)
                granted -> settings.set(SearchSettings.Contacts, true)
                else -> request.launch(Manifest.permission.READ_CONTACTS)
            }
        },
        title = R.string.settings_search_contacts,
        summary = R.string.settings_search_contacts_short,
        icon = R.drawable.sym_person,
    )
}
