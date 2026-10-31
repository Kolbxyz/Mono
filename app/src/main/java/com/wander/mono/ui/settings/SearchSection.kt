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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.settings.SearchSettings

/**
 * What search looks in besides apps (a source switched off is not even asked): the everyday
 * sources at a glance, then the two that read personal data, each with its access, then whether
 * recent searches are kept (and, once some are, clearing them; switching it off forgets them too).
 */
@Composable
fun SearchSection(settings: SettingsViewModel, history: SearchHistoryViewModel = viewModel()) {
    val remembering = settings.collect(SearchSettings.RememberRecent) ?: return
    val hasHistory by history.hasHistory.collectAsStateWithLifecycle(initialValue = false)
    SettingsGroup(R.string.settings_search_sources) {
        ToggleChipsSetting(
            settings,
            title = R.string.settings_search_finds,
            icon = R.drawable.sym_search,
            chips = listOf(
                ToggleChip(SearchSettings.QuickAnswers, R.string.search_source_quick_answers, R.drawable.sym_calculate),
                ToggleChip(SearchSettings.Shortcuts, R.string.search_source_shortcuts, R.drawable.sym_arrow_outward),
                ToggleChip(
                    SearchSettings.SettingsPages,
                    R.string.search_source_settings_pages,
                    R.drawable.sym_settings,
                ),
            ),
        )
        ContactsSwitch(settings)
        SwitchSetting(
            settings,
            SearchSettings.Notifications,
            title = R.string.search_source_notifications,
            summary = R.string.search_source_notifications_summary,
            icon = R.drawable.sym_notifications,
        )
        SwitchRow(
            checked = remembering,
            onCheckedChange = history::setRemembering,
            title = R.string.search_remember_recent,
            summary = R.string.search_remember_recent_summary,
            icon = R.drawable.sym_schedule,
        )
        if (hasHistory) {
            ActionRow(title = R.string.search_clear_history, icon = R.drawable.sym_delete, onClick = history::clear)
        }
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
