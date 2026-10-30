package com.wander.mono.data

import com.wander.mono.core.apps.AppKey

/** What swiping a favourite left does; no stored action means the swipe does nothing. */
sealed interface SwipeAction {
    /** Opens the app's page in Android's settings. */
    data object AppInfo : SwipeAction

    /** Opens the app's shortcut [id], one it publishes. */
    data class Shortcut(val id: String) : SwipeAction

    /** As stored after the app's key: never changed, or saved choices are lost. */
    fun encode(): String = when (this) {
        AppInfo -> INFO
        is Shortcut -> SHORTCUT_PREFIX + id
    }

    companion object {
        private const val INFO = "info"
        private const val SHORTCUT_PREFIX = "shortcut:"

        fun decode(encoded: String): SwipeAction? = when {
            encoded == INFO -> AppInfo
            encoded.startsWith(SHORTCUT_PREFIX) && encoded.length > SHORTCUT_PREFIX.length ->
                Shortcut(encoded.removePrefix(SHORTCUT_PREFIX))
            else -> null
        }
    }
}

/** One favourite's swipe, as persisted: the app key, then the action (shortcut ids may hold any character). */
internal object SwipeEntry {
    // App keys use '/', never '|'; the first '|' ends the key.
    private const val SEPARATOR = '|'

    fun encode(app: AppKey, action: SwipeAction): String = "${app.encode()}$SEPARATOR${action.encode()}"

    fun decode(encoded: String): Pair<AppKey, SwipeAction>? {
        val split = encoded.indexOf(SEPARATOR)
        if (split < 0) return null
        val app = AppKey.decode(encoded.substring(0, split))
        val action = SwipeAction.decode(encoded.substring(split + 1))
        return if (app != null && action != null) app to action else null
    }
}
