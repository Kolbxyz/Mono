package com.wander.mono.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import com.wander.mono.core.security.HiddenAppsLock
import com.wander.mono.data.backup.Backup
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

/**
 * Saving and restoring a backup file. A file is read and checked whole before anything changes,
 * then held in [pending] until the user confirms it replaces their choices. With hidden apps
 * locked, both ways need the screen lock first: a backup lists hidden apps and can unhide them.
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

    fun unlock() = lock.unlock()

    /** [uri] is null when the user backed out of the file picker. */
    fun export(uri: Uri?) {
        uri ?: return
        viewModelScope.launch {
            if (messages.report(backups.export(uri))) messages.show(R.string.backup_saved)
        }
    }

    fun read(uri: Uri?) {
        uri ?: return
        viewModelScope.launch {
            when (val read = backups.read(uri)) {
                is BackupRead.Failed -> messages.show(read.error.message)
                is BackupRead.Read -> mutablePending.value = read.backup
            }
        }
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

    private companion object {
        const val STOP_MS = 5_000L
    }
}
