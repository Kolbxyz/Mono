package com.wander.mono.core.siblings

/**
 * Names fixed by docs/design/sibling-contract.md, mirrored from the siblings' own contract files.
 * Columns are read by name, so a sibling adding columns never breaks Mono.
 */
internal object SiblingContract {
    const val PERMISSION = "com.wander.permission.SIBLING_READ"

    /** Every sibling publishes its provider as `${applicationId}.mono`. */
    fun authorityOf(packageName: String) = "$packageName$AUTHORITY_SUFFIX"

    fun packageOf(authority: String) = authority.removeSuffix(AUTHORITY_SUFFIX)

    const val EXTRA_SNAPSHOT_AT_MS = "snapshot_at_ms"
    const val PARAM_LIMIT = "limit"
    const val PARAM_QUERY = "q"

    private const val AUTHORITY_SUFFIX = ".mono"

    object Wanda {
        /** Release first: a phone with both builds gets the one the user lives in. */
        val packages = listOf("com.wander.android", "com.wander.android.debug")
        const val PATH_UP_NEXT = "up_next"
        const val PATH_SEARCH = "search"

        const val QUEUE_ID = "queue_id"
        const val TITLE = "title"
        const val ARTIST = "artist"
        const val TRACK_ID = "track_id"
        const val PLAY_URI = "play_uri"
    }

    object Phalanx {
        val packages = listOf("org.phalanx.android", "org.phalanx.android.debug")
        const val PATH_ROOMS = "rooms"

        const val ROOM_ID = "room_id"
        const val UNREAD_COUNT = "unread_count"
        const val IS_MARKED_UNREAD = "is_marked_unread"
    }
}
