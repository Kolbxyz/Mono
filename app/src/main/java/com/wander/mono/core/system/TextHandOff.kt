package com.wander.mono.core.system

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.annotation.StringRes
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hands what was pasted into search to another app: a web address to the app that opens it (the
 * browser, or the app that claims the link), any text to Android's share sheet. Mono only passes it
 * on; nothing is fetched here.
 */
@Singleton
class TextHandOff @Inject constructor(@ApplicationContext private val context: Context) {
    /** Whether an app opens [url]. Resolves an intent, so not on the main thread. */
    fun canOpen(url: String): Boolean =
        context.packageManager.resolveActivity(viewIntent(url), PackageManager.MATCH_DEFAULT_ONLY) != null

    fun open(url: String): ActionResult = start(viewIntent(url), R.string.search_error_no_link_app)

    /** Android's share sheet, so the user picks where [text] goes every time. */
    fun share(text: String): ActionResult {
        val send = Intent(Intent.ACTION_SEND).setType(PLAIN_TEXT).putExtra(Intent.EXTRA_TEXT, text)
        return start(Intent.createChooser(send, null), R.string.search_error_no_share_app)
    }

    private fun viewIntent(url: String) =
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE)

    private fun start(intent: Intent, @StringRes failure: Int): ActionResult = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        ActionResult.Done
    } catch (_: ActivityNotFoundException) {
        ActionResult.Failed(failure)
    }

    private companion object {
        const val PLAIN_TEXT = "text/plain"
    }
}
