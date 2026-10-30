package com.wander.mono.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import com.wander.mono.core.security.HiddenAppsLock
import com.wander.mono.data.backup.Backup
import com.wander.mono.data.backup.BackupError
import com.wander.mono.data.backup.BackupRead
import com.wander.mono.data.backup.Backups
import com.wander.mono.ui.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** A sealed backup waiting for its passphrase: [wrong] after one that did not open it, [opening] while one is tried. */
data class SealedPrompt(val wrong: Boolean = false, val opening: Boolean = false)

/**
 * Saving and restoring a backup file. A file is read and checked whole before anything changes,
 * then held in [pending] until the user confirms it replaces their choices; a sealed one first
 * waits in [sealed] for its passphrase. With hidden apps locked, both ways need the screen lock
 * first: a backup lists hidden apps and can unhide them. Passphrases live in memory only, as
 * character arrays wiped once used.
 */
@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backups: Backups,
    private val lock: HiddenAppsLock,
    private val messages: UserMessages,
) : ViewModel() {
    val revealed: StateFlow<Boolean?> =
        lock.revealed.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_MS), null)

    private val mutablePending = MutableStateFlow<Backup?>(null)
    val pending: StateFlow<Backup?> = mutablePending.asStateFlow()

    private val mutableSealed = MutableStateFlow<SealedPrompt?>(null)
    val sealed: StateFlow<SealedPrompt?> = mutableSealed.asStateFlow()

    private var sealedFile: ByteArray? = null
    private var exportPassphrase: CharArray? = null

    fun unlock() = lock.unlock()

    /** The passphrase the next sealed export uses, set just before its file picker opens. */
    fun sealNextExport(passphrase: CharArray) {
        exportPassphrase?.fill(Char(0))
        exportPassphrase = passphrase
    }

    /** [uri] is null when the user backed out of the file picker. */
    fun export(uri: Uri?) = save(uri, passphrase = null)

    /** As [export], sealed. A passphrase lost with the process is asked for again, never skipped. */
    fun exportSealed(uri: Uri?) {
        val passphrase = exportPassphrase
        exportPassphrase = null
        if (uri != null && passphrase == null) {
            messages.report(ActionResult.Failed(R.string.backup_error_passphrase_lost))
        } else {
            save(uri, passphrase)
        }
    }

    fun read(uri: Uri?) {
        uri ?: return
        viewModelScope.launch { show(backups.read(uri)) }
    }

    /** Tries [passphrase] on the sealed file; a wrong one keeps the prompt open, marked. */
    fun openSealed(passphrase: CharArray) {
        val file = sealedFile ?: return
        mutableSealed.value = SealedPrompt(opening = true)
        viewModelScope.launch {
            val read = try {
                backups.open(file, passphrase)
            } finally {
                passphrase.fill(Char(0))
            }
            if (read is BackupRead.Failed && read.error == BackupError.WrongPassphrase) {
                mutableSealed.value = SealedPrompt(wrong = true)
            } else {
                cancelSealed()
                show(read)
            }
        }
    }

    fun cancelSealed() {
        sealedFile = null
        mutableSealed.value = null
    }

    fun confirmRestore() {
        val backup = mutablePending.value ?: return
        mutablePending.value = null
        viewModelScope.launch {
            if (messages.report(backups.restore(backup))) messages.show(R.string.backup_restored)
        }
    }

    fun cancelRestore() {
        mutablePending.value = null
    }

    fun reportNoScreenLock() {
        messages.report(ActionResult.Failed(R.string.error_no_screen_lock))
    }

    fun reportNoPicker() {
        messages.report(ActionResult.Failed(R.string.backup_error_no_picker))
    }

    override fun onCleared() {
        exportPassphrase?.fill(Char(0))
    }

    private fun save(uri: Uri?, passphrase: CharArray?) {
        if (uri == null) {
            passphrase?.fill(Char(0))
            return
        }
        viewModelScope.launch {
            try {
                if (messages.report(backups.export(uri, passphrase))) messages.show(R.string.backup_saved)
            } finally {
                passphrase?.fill(Char(0))
            }
        }
    }

    private fun show(read: BackupRead) {
        when (read) {
            is BackupRead.Failed -> messages.show(read.error.message)
            is BackupRead.Read -> mutablePending.value = read.backup
            is BackupRead.Sealed -> {
                sealedFile = read.file
                mutableSealed.value = SealedPrompt()
            }
        }
    }

    private companion object {
        const val STOP_MS = 5_000L
    }
}
