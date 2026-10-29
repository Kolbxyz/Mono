package com.wander.mono.data.backup

import androidx.annotation.StringRes
import com.wander.mono.R
import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.LiveRowRef
import com.wander.mono.data.settings.Setting
import com.wander.mono.data.settings.SettingValue

/** Why a file could not be restored; nothing is changed in any of these cases. */
enum class BackupError(@param:StringRes val message: Int) {
    Unreadable(R.string.backup_error_unreadable),
    NotABackup(R.string.backup_error_not_backup),
    Newer(R.string.backup_error_newer),
    Malformed(R.string.backup_error_malformed),
}

/** A file read as a [Backup], or the reason it could not be. */
sealed interface BackupRead {
    data class Read(val backup: Backup) : BackupRead

    data class Failed(val error: BackupError) : BackupRead
}

/**
 * The backup file's layout, as a JSON-shaped tree of maps, lists, strings, numbers and booleans.
 * App keys and live rows keep the encodings the stores use. A [VERSION] newer than this build's is
 * refused whole, never half read; a setting this build does not know is ignored, and one the file
 * lacks goes back to its default.
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
        "settings" to backup.settings.associate { it.setting.name to it.stored },
    )

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
            liveRowsOff = tree.strings("liveRowsOff").mapTo(mutableSetOf(), ::liveRow),
            quietApps = tree.strings("quietApps").mapTo(mutableSetOf(), ::appKey),
            settings = settings.map { setting -> settingValue(setting, stored[setting.name]) },
        )
    }

    private fun <T> settingValue(setting: Setting<T>, stored: Any?): SettingValue<T> = when (stored) {
        null -> SettingValue.default(setting)
        else -> SettingValue.parse(setting, stored) ?: throw MalformedBackup()
    }

    private fun appKey(encoded: Any?): AppKey = AppKey.decode(string(encoded)) ?: throw MalformedBackup()

    private fun liveRow(encoded: String): LiveRowRef = LiveRowRef.decode(encoded) ?: throw MalformedBackup()

    private fun string(value: Any?): String = value as? String ?: throw MalformedBackup()

    private inline fun <reified T> Map<String, Any?>.field(name: String): T =
        this[name] as? T ?: throw MalformedBackup()

    private fun Map<String, Any?>.strings(name: String): List<String> = field<List<*>>(name).map(::string)

    private fun Number.whole(): Long = integer(this) ?: throw MalformedBackup()

    /** Whole numbers only: JSON parsers hand them over as Int or Long depending on size. */
    private fun integer(value: Any?): Long? = when (value) {
        is Int -> value.toLong()
        is Long -> value
        else -> null
    }

    /** A field missing or of the wrong type; caught in [decode] only. */
    private class MalformedBackup : Exception()
}
