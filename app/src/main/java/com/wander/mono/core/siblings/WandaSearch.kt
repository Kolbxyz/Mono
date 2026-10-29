package com.wander.mono.core.siblings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import com.wander.mono.core.siblings.SiblingContract.Wanda
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Songs from Wanda's local library for drawer search. Wanda never searches online for Mono, and
 * nothing is kept here: each query asks afresh, like contacts.
 */
@Singleton
class WandaSearch @Inject constructor(
    @ApplicationContext private val context: Context,
    private val providers: SiblingProviders,
) {
    /** At most [limit] tracks for [query], ordered by Wanda (most played first); empty when Wanda is absent. */
    suspend fun find(query: String, limit: Int): List<WandaTrack> {
        if (query.isBlank()) return emptyList()
        val authority = withContext(Dispatchers.IO) { providers.authority(Wanda.packages) } ?: return emptyList()
        val packageName = SiblingContract.packageOf(authority)
        val uri = Uri.Builder().scheme("content").authority(authority).path(Wanda.PATH_SEARCH)
            .appendQueryParameter(SiblingContract.PARAM_QUERY, query.trim())
            .appendQueryParameter(SiblingContract.PARAM_LIMIT, limit.toString())
            .build()
        return providers.query(uri) { cursor -> cursor.mapRows { SiblingRows.track(it, packageName) } }.orEmpty()
    }

    /** Hands the track's link to the Wanda build that found it, which plays it. */
    fun open(track: WandaTrack): ActionResult = try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(track.playUri))
            .setPackage(track.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        ActionResult.Done
    } catch (_: ActivityNotFoundException) {
        ActionResult.Failed(R.string.error_wanda_missing)
    }
}
