package com.wander.mono.data.backup

import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.LiveRowRef
import com.wander.mono.data.settings.SettingValue

/**
 * Everything a backup file restores: the home list, hidden apps, custom names, switched-off live
 * rows, quiet apps, headphone apps and settings. Widgets are not in it: their ids are bound to this phone's host.
 * [mainSerial] is the serial of the profile the backup was made in, so keys can be moved to
 * another phone's profiles with [remapped].
 */
data class Backup(
    val mainSerial: Long,
    val favorites: List<AppKey>,
    val hidden: Set<AppKey>,
    val labels: Map<AppKey, String>,
    val liveRowsOff: Set<LiveRowRef>,
    val quietApps: Set<AppKey>,
    val headphoneApps: Set<AppKey>,
    val settings: List<SettingValue<*>>,
) {
    /** Every profile serial the backup's app keys name. */
    val serials: Set<Long>
        get() = (favorites + hidden + labels.keys + liveRowsOff.map { it.app } + quietApps + headphoneApps)
            .mapTo(mutableSetOf()) { it.userSerial }

    /** The same backup with each key moved to the serial [serialOf] gives; keys it maps to null are dropped. */
    fun remapped(mainSerial: Long, serialOf: (Long) -> Long?): Backup {
        fun AppKey.moved(): AppKey? = serialOf(userSerial)?.let { copy(userSerial = it) }
        return copy(
            mainSerial = mainSerial,
            favorites = favorites.mapNotNull { it.moved() }.distinct(),
            hidden = hidden.mapNotNullTo(mutableSetOf()) { it.moved() },
            labels = labels.entries.mapNotNull { (key, label) -> key.moved()?.let { it to label } }.toMap(),
            liveRowsOff = liveRowsOff.mapNotNullTo(mutableSetOf()) { row ->
                row.app.moved()?.let { row.copy(app = it) }
            },
            quietApps = quietApps.mapNotNullTo(mutableSetOf()) { it.moved() },
            headphoneApps = headphoneApps.mapNotNullTo(mutableSetOf()) { it.moved() },
        )
    }
}

/**
 * Where a backup's profile serials go on this phone, whose own profile is [main] and whose other
 * profiles (work, private space) are [others]. The backup's own profile becomes [main]; a serial
 * this phone also has stays; otherwise, when both sides have exactly one other profile, one
 * becomes the other. Anything else has no profile here and maps to null.
 */
internal fun profileRemap(backupMain: Long, backupSerials: Set<Long>, main: Long, others: Set<Long>): (Long) -> Long? {
    val onlyOther = others.singleOrNull()?.takeIf { (backupSerials - backupMain).size == 1 }
    return { serial ->
        when (serial) {
            backupMain -> main
            in others -> serial
            else -> onlyOther
        }
    }
}
