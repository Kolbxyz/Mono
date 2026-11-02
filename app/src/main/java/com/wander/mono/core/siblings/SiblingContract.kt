package com.wander.mono.core.siblings

import android.content.Context
import android.content.pm.ApplicationInfo

/**
 * Names fixed by docs/design/sibling-contract.md, mirrored from the siblings' own contract files.
 * Columns are read by name, so a sibling adding columns never breaks Mono.
 */
internal object SiblingContract {
    /**
     * The signature permission guarding the providers. Debug builds use their own name, so a debug
     * build (signed with a debug key) never redeclares the release one: Android refuses to install
     * an app that does. Must match `siblingPermission` in app/build.gradle.kts.
     */
    fun permission(context: Context): String =
        if (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) DEBUG_PERMISSION else PERMISSION

    private const val PERMISSION = "com.wander.permission.SIBLING_READ"
    private const val DEBUG_PERMISSION = "com.wander.permission.SIBLING_READ_DEBUG"

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
