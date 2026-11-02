package com.wander.mono.data.backup

import androidx.annotation.StringRes
import com.wander.mono.R
import com.wander.mono.data.LiveRowRef
import com.wander.mono.data.settings.Setting
import com.wander.mono.data.settings.SettingValue

/** Why a file could not be restored; nothing is changed in any of these cases. */
enum class BackupError(@param:StringRes val message: Int) {
    Unreadable(R.string.backup_error_unreadable),
    NotABackup(R.string.backup_error_not_backup),
    Newer(R.string.backup_error_newer),
    Malformed(R.string.backup_error_malformed),
    WrongPassphrase(R.string.backup_error_passphrase),
}

/** A file read as a [Backup], or the reason it could not be. */
sealed interface BackupRead {
    data class Read(val backup: Backup) : BackupRead

    data class Failed(val error: BackupError) : BackupRead

    /** Sealed with a passphrase: nothing is read until it is given. */
    class Sealed(val file: ByteArray) : BackupRead
}

/**
 * The backup file's layout, as a JSON-shaped tree of maps, lists, strings, numbers and booleans.
 * App keys and live rows keep the encodings the stores use. A [VERSION] newer than this build's is
 * refused whole, never half read; a setting this build does not know is ignored, and one the file
 * lacks goes back to its default. Locked apps and home's structure (folders and the rest, see
 * [StructureFormat]) came later in the same version: a file without them restores none, and an
 * older build skips them.
 */
internal object BackupFormat {
    const val FORMAT = "mono-backup"
    const val VERSION = 1

    fun encode(backup: Backup): Map<String, Any> = mapOf(
        "format" to FORMAT,
        "version" to VERSION,
        "mainProfile" to backup.mainSerial,
        "favorites" to backup.favorites.map { it.encode() },
        "hidden" to backup.hidden.map { it.encode() },
        "labels" to backup.labels.entries.associate { (key, label) -> key.encode() to label },
        "liveRowsOff" to backup.liveRowsOff.map { it.encode() },
        "quietApps" to backup.quietApps.map { it.encode() },
        "headphoneApps" to backup.headphoneApps.map { it.encode() },
        "settings" to backup.settings.associate { it.setting.name to it.stored },
        "lockedApps" to backup.lockedApps.map { it.encode() },
    ) + StructureFormat.encode(backup.structure)

    fun decode(tree: Map<String, Any?>, settings: List<Setting<*>>): BackupRead {
        if (tree["format"] != FORMAT) return BackupRead.Failed(BackupError.NotABackup)
        val version = integer(tree["version"])
        return when {
            version == null || version < 1 -> BackupRead.Failed(BackupError.Malformed)
            version > VERSION -> BackupRead.Failed(BackupError.Newer)
            else -> try {
                BackupRead.Read(read(tree, settings))
            } catch (_: MalformedBackup) {
                BackupRead.Failed(BackupError.Malformed)
            }
        }
    }

    private fun read(tree: Map<String, Any?>, settings: List<Setting<*>>): Backup {
        val stored = tree.field<Map<*, *>>("settings")
        return Backup(
            mainSerial = tree.field<Number>("mainProfile").whole(),
            favorites = tree.strings("favorites").map(::appKey).distinct(),
            hidden = tree.strings("hidden").mapTo(mutableSetOf(), ::appKey),
            labels = tree.field<Map<*, *>>("labels").entries.associate { (key, label) -> appKey(key) to string(label) },
            liveRowsOff = tree.strings("liveRowsOff").filterNot(LiveRowRef::isRetired).mapTo(mutableSetOf(), ::liveRow),
            quietApps = tree.strings("quietApps").mapTo(mutableSetOf(), ::appKey),
            // Added after the first version 1 files were written: absent reads as none chosen.
            headphoneApps = tree.optionalField<List<*>>("headphoneApps", emptyList<Any?>())
                .mapTo(mutableSetOf(), ::appKey),
            settings = settings.map { setting -> settingValue(setting, stored[setting.name]) },
            lockedApps = tree.optionalField<List<*>>("lockedApps", emptyList<Any?>()).mapTo(mutableSetOf(), ::appKey),
            structure = StructureFormat.read(tree),
        )
    }

    private fun <T> settingValue(setting: Setting<T>, stored: Any?): SettingValue<T> = when (stored) {
        null -> SettingValue.default(setting)
        else -> SettingValue.parse(setting, stored) ?: throw MalformedBackup()
    }

    private fun liveRow(encoded: String): LiveRowRef = LiveRowRef.decode(encoded) ?: throw MalformedBackup()
}
