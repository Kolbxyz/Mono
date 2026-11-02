package com.wander.mono.core.siblings

import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import com.wander.mono.core.siblings.SiblingContract.authorityOf
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Finds and reads the siblings' providers. A sibling that is not installed, does not publish the
 * provider yet, or is signed with another key (so the signature permission is not granted) is
 * simply absent: every read answers null and nothing is shown.
 */
@Singleton
class SiblingProviders @Inject constructor(@ApplicationContext private val context: Context) {
    /** The provider authority of the first of [packages] that serves one Mono may read, or null. */
    fun authority(packages: List<String>): String? {
        val permission = SiblingContract.permission(context)
        if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) return null
        return packages.map(::authorityOf).firstOrNull { context.packageManager.resolveContentProvider(it, 0) != null }
    }

    /** One read of [uri] on the IO dispatcher; null when the provider is gone or refuses Mono. */
    suspend fun <T> query(uri: Uri, read: (Cursor) -> T): T? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use(read)
        } catch (_: SecurityException) {
            // Signed with another key, or the permission was redefined: the sibling is absent.
            null
        } catch (_: IllegalArgumentException) {
            // An older sibling that does not serve this path yet ("Unknown URI"): absent too.
            null
        }
    }

    /**
     * [uri] read now and again each time the sibling calls `notifyChange` on it, while collected.
     * Bursts of changes are conflated, so a slow read never queues up stale ones.
     */
    fun <T> observe(uri: Uri, read: (Cursor) -> T): Flow<T?> = changes(uri).map { query(uri, read) }

    /** Emits once at start, then on every change; nothing is polled. */
    private fun changes(uri: Uri): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        trySend(Unit)
        try {
            context.contentResolver.registerContentObserver(uri, true, observer)
        } catch (_: SecurityException) {
            // Not ours to watch: the one read above already answers null.
            close()
        }
        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }
        .conflate()
        .flowOn(Dispatchers.IO)
}
