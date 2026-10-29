package com.wander.mono.core.apps

/**
 * One launchable activity in one user profile. The profile is its serial number, which, unlike a
 * `UserHandle`, is stable across reboots, so a key can be persisted.
 */
data class AppKey(val packageName: String, val className: String, val userSerial: Long) {
    fun encode(): String = "$packageName$SEPARATOR$className$SEPARATOR$userSerial"

    companion object {
        // Neither package nor class names may contain a slash.
        private const val SEPARATOR = '/'
        private const val PARTS = 3

        fun decode(encoded: String): AppKey? {
            val parts = encoded.split(SEPARATOR)
            val serial = parts.getOrNull(2)?.toLongOrNull()
            return if (parts.size == PARTS && serial != null) AppKey(parts[0], parts[1], serial) else null
        }
    }
}
