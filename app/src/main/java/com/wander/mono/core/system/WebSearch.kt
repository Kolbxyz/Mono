package com.wander.mono.core.system

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** The app that would take a web search; [appLabel] is null when Android would ask which one. */
data class WebSearchTarget(val appLabel: String?)

/**
 * Hands a query to the browser with `ACTION_WEB_SEARCH`. Mono itself never touches the network:
 * the browser does the searching, and only when the user taps.
 */
@Singleton
class WebSearch @Inject constructor(@ApplicationContext private val context: Context) {
    /** Null when nothing on the phone handles web searches. Resolves an intent, so not on the main thread. */
    fun target(): WebSearchTarget? {
        val pm = context.packageManager
        val info = pm.resolveActivity(Intent(Intent.ACTION_WEB_SEARCH), PackageManager.MATCH_DEFAULT_ONLY)
            ?: return null
        // With no default set, the system's chooser answers rather than a browser.
        val chooser = info.activityInfo.packageName == SYSTEM_PACKAGE
        return WebSearchTarget(if (chooser) null else info.loadLabel(pm).toString())
    }

    fun search(query: String): ActionResult = try {
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
            .putExtra(SearchManager.QUERY, query)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        ActionResult.Done
    } catch (_: ActivityNotFoundException) {
        ActionResult.Failed(R.string.error_no_browser)
    }

    private companion object {
        const val SYSTEM_PACKAGE = "android"
    }
}
