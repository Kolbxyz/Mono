package com.wander.mono.core.siblings

import android.database.Cursor
import android.net.Uri
import com.wander.mono.core.apps.Profiles
import com.wander.mono.core.siblings.SiblingContract.Phalanx
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Unread per room as Phalanx itself counts it, muted rooms included. Only the Phalanx in Mono's own
 * profile is reachable (a provider answers for its user), so [packageName] and [userSerial] say
 * which conversation shortcuts these counts belong to.
 */
data class PhalanxUnread(val packageName: String, val userSerial: Long, val rooms: Map<String, RoomUnread>)

@Singleton
class PhalanxRooms @Inject constructor(private val providers: SiblingProviders, private val profiles: Profiles) {
    /**
     * Phalanx's rooms with something unread, re-read whenever Phalanx reports a change, while
     * collected. Null when Phalanx is absent or its snapshot is too old to trust.
     */
    val unread: Flow<PhalanxUnread?> = flow {
        val authority = providers.authority(Phalanx.packages)
        if (authority == null) {
            emit(null)
        } else {
            val uri = Uri.Builder().scheme("content").authority(authority).path(Phalanx.PATH_ROOMS).build()
            val owner = SiblingContract.packageOf(authority)
            val serial = profiles.serialOf(profiles.main)
            emitAll(providers.observe(uri) { cursor -> read(cursor, owner, serial) })
        }
    }.flowOn(Dispatchers.IO)

    private fun read(cursor: Cursor, owner: String, serial: Long): PhalanxUnread? {
        val snapshotAt = cursor.extras.getLong(SiblingContract.EXTRA_SNAPSHOT_AT_MS, 0L)
        if (!SiblingRows.isFresh(snapshotAt, System.currentTimeMillis())) return null
        val rooms = cursor.mapRows(SiblingRows::room).associateBy { it.roomId }
        return PhalanxUnread(owner, serial, rooms)
    }
}
