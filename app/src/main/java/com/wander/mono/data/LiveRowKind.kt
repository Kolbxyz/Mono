package com.wander.mono.data

import androidx.annotation.StringRes
import com.wander.mono.R
import com.wander.mono.core.apps.AppKey
import com.wander.mono.data.settings.StoredEnum

/**
 * The kinds of live row that can hang under a favourite. Each can be switched off per app in edit
 * mode. [stored] is what is saved, so a constant can be renamed but its [stored] value never changed.
 */
enum class LiveRowKind(override val stored: String, @param:StringRes val title: Int) : StoredEnum {
    NowPlaying("now_playing", R.string.live_row_now_playing),
    NotificationPreview("notification_preview", R.string.live_row_notification_preview),
    Calendar("calendar", R.string.live_row_calendar),
}

/** One live row of one app, as persisted when it is switched off. */
data class LiveRowRef(val app: AppKey, val kind: LiveRowKind) {
    fun encode(): String = "${kind.stored}$SEPARATOR${app.encode()}"

    companion object {
        // App keys use '/', never '|'.
        private const val SEPARATOR = '|'

        fun decode(encoded: String): LiveRowRef? {
            val kind = LiveRowKind.entries.firstOrNull { encoded.startsWith(it.stored + SEPARATOR) } ?: return null
            val app = AppKey.decode(encoded.substring(kind.stored.length + 1)) ?: return null
            return LiveRowRef(app, kind)
        }
    }
}
