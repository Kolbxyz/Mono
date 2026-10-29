package com.wander.mono.data.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import com.wander.mono.core.apps.Profiles
import com.wander.mono.core.quiet.QuietMode
import com.wander.mono.data.CustomLabels
import com.wander.mono.data.LauncherPrefs
import com.wander.mono.data.LiveRowPrefs
import com.wander.mono.data.QuietApps
import com.wander.mono.data.settings.SettingsStore
import com.wander.mono.data.settings.backedUpSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Saves the user's choices to a file they picked and reads them back, through the Storage Access
 * Framework: Mono never sees a path or needs a storage permission, and nothing leaves the phone
 * unless the user puts the file somewhere that syncs.
 */
@Singleton
class Backups @Inject constructor(
    @ApplicationContext context: Context,
    private val launcherPrefs: LauncherPrefs,
    private val customLabels: CustomLabels,
    private val liveRows: LiveRowPrefs,
    private val quietApps: QuietApps,
    private val settings: SettingsStore,
    private val quietMode: QuietMode,
    private val profiles: Profiles,
) {
    private val resolver: ContentResolver = context.contentResolver

    suspend fun export(uri: Uri): ActionResult = withContext(Dispatchers.IO) {
        val text = BackupJson.write(BackupFormat.encode(current()))
        try {
            val stream = resolver.openOutputStream(uri, "wt") ?: return@withContext WRITE_FAILED
            stream.use { it.write(text.toByteArray()) }
            ActionResult.Done
        } catch (_: IOException) {
            WRITE_FAILED
        } catch (_: SecurityException) {
            WRITE_FAILED
        }
    }

    /** The backup in [uri], checked whole and moved to this phone's profiles; nothing is changed yet. */
    suspend fun read(uri: Uri): BackupRead = withContext(Dispatchers.Default) {
        val text = withContext(Dispatchers.IO) { readText(uri) } ?: return@withContext UNREADABLE
        val tree = BackupJson.read(text) ?: return@withContext BackupRead.Failed(BackupError.NotABackup)
        when (val read = BackupFormat.decode(tree, backedUpSettings)) {
            is BackupRead.Failed -> read
            is BackupRead.Read -> BackupRead.Read(toThisPhone(read.backup))
        }
    }

    /**
     * Replaces the user's choices with [backup]'s. Each store is written in one edit; the linked Do
     * Not Disturb mode is then brought in line with the restored setting.
     */
    suspend fun restore(backup: Backup): ActionResult = try {
        launcherPrefs.replace(backup.favorites, backup.hidden)
        customLabels.replace(backup.labels)
        liveRows.replace(backup.liveRowsOff)
        quietApps.replace(backup.quietApps)
        settings.restore(backup.settings)
        quietMode.setLinksDoNotDisturb(quietMode.linksDoNotDisturb.first())
        ActionResult.Done
    } catch (_: IOException) {
        ActionResult.Failed(R.string.backup_error_restore)
    }

    private suspend fun current() = Backup(
        mainSerial = profiles.serialOf(profiles.main),
        favorites = launcherPrefs.favorites.first(),
        hidden = launcherPrefs.hidden.first(),
        labels = customLabels.labels.first(),
        liveRowsOff = liveRows.hidden.first(),
        quietApps = quietApps.apps.first(),
        settings = settings.snapshot(backedUpSettings),
    )

    private fun toThisPhone(backup: Backup): Backup {
        val main = profiles.serialOf(profiles.main)
        val remap = profileRemap(backup.mainSerial, backup.serials, main, profiles.otherSerials())
        return backup.remapped(main, remap)
    }

    /** The file's text, or null when it cannot be opened or is far larger than any backup. */
    private fun readText(uri: Uri): String? = try {
        resolver.openInputStream(uri)?.use(::readCapped)
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    }

    private fun readCapped(stream: InputStream): String? {
        val bytes = stream.readNBytesCompat(MAX_BYTES + 1)
        return if (bytes.size > MAX_BYTES) null else bytes.decodeToString()
    }

    /** `InputStream.readNBytes` is Java 11 (API 33); this reads at most [limit] bytes on any API level. */
    private fun InputStream.readNBytesCompat(limit: Int): ByteArray {
        val buffer = ByteArray(limit)
        var read = 0
        while (read < limit) {
            val count = read(buffer, read, limit - read)
            if (count < 0) break
            read += count
        }
        return buffer.copyOf(read)
    }

    private companion object {
        /** A backup of hundreds of apps is a few tens of kilobytes. */
        const val MAX_BYTES = 1 shl 20
        val WRITE_FAILED = ActionResult.Failed(R.string.backup_error_write)
        val UNREADABLE = BackupRead.Failed(BackupError.Unreadable)
    }
}
