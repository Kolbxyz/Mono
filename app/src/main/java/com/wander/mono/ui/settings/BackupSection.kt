package com.wander.mono.ui.settings

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.ui.components.rememberCredentialPrompt
import java.time.LocalDate

/** What the backup rows do once hidden apps are unlocked. */
private enum class BackupStep { Export, Import }

/**
 * Save to and restore from a file the user picks in Android's file picker. Saving offers a
 * passphrase first; restoring a sealed file asks for it, and asks again before replacing
 * favourites and settings. With hidden apps locked, both ask for the screen lock.
 */
@Composable
fun BackupSection(backup: BackupViewModel = viewModel()) {
    val revealed by backup.revealed.collectAsStateWithLifecycle()
    val pending by backup.pending.collectAsStateWithLifecycle()
    val sealed by backup.sealed.collectAsStateWithLifecycle()
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(MIME_JSON), backup::export)
    val saveSealed =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(MIME_SEALED), backup::exportSealed)
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument(), backup::read)
    var choosingSeal by rememberSaveable { mutableStateOf(false) }
    val picker: (() -> Unit) -> Unit = { launch ->
        try {
            launch()
        } catch (_: ActivityNotFoundException) {
            backup.reportNoPicker()
        }
    }
    val run: (BackupStep) -> Unit = { step ->
        when (step) {
            BackupStep.Export -> { choosingSeal = true }
            BackupStep.Import -> picker { open.launch(OPEN_TYPES) }
        }
    }
    var afterUnlock by rememberSaveable { mutableStateOf<BackupStep?>(null) }
    val prompt = rememberCredentialPrompt(
        R.string.backup_unlock_title,
        onSuccess = {
            backup.unlock()
            afterUnlock?.let(run)
            afterUnlock = null
        },
        onNoScreenLock = backup::reportNoScreenLock,
    )
    val guarded: (BackupStep) -> Unit = { step ->
        if (revealed == true) {
            run(step)
        } else {
            afterUnlock = step
            prompt()
        }
    }
    SettingsGroup(R.string.settings_backup) {
        ActionRow(R.string.settings_backup_export, R.string.settings_backup_export_summary) {
            guarded(BackupStep.Export)
        }
        ActionRow(R.string.settings_backup_import, R.string.settings_backup_import_summary) {
            guarded(BackupStep.Import)
        }
    }
    if (choosingSeal) {
        SealBackupDialog(
            onSeal = { passphrase ->
                choosingSeal = false
                backup.sealNextExport(passphrase)
                picker { saveSealed.launch("${fileName()}.$SEALED_EXTENSION") }
            },
            onPlain = {
                choosingSeal = false
                picker { save.launch("${fileName()}.json") }
            },
            onDismiss = { choosingSeal = false },
        )
    }
    sealed?.let { prompt -> OpenSealedDialog(prompt, onOpen = backup::openSealed, onDismiss = backup::cancelSealed) }
    if (pending != null) RestoreDialog(onConfirm = backup::confirmRestore, onDismiss = backup::cancelRestore)
}

private fun fileName() = "mono-backup-${LocalDate.now()}"

@Composable
private fun RestoreDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.backup_restore_title)) },
        text = { Text(stringResource(R.string.backup_restore_text)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.backup_restore_confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.backup_restore_cancel)) } },
    )
}

private const val MIME_JSON = "application/json"

/** A sealed backup is not JSON to anything but Mono: plain bytes, under Mono's own extension. */
private const val MIME_SEALED = "application/octet-stream"
private const val SEALED_EXTENSION = "monobackup"

/** File managers label JSON inconsistently; the file is checked whole on reading anyway. */
private val OPEN_TYPES = arrayOf(MIME_JSON, "text/plain", "application/octet-stream")
