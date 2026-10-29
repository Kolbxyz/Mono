package com.wander.mono.core.notifications

import android.app.ActivityOptions
import android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Opening, dismissing and replying to a notification from its preview row. */
@Singleton
class NotificationActions @Inject constructor(
    @ApplicationContext private val context: Context,
    private val previews: NotificationPreviews,
) {
    /** Sends the notification's own tap intent; null when it has none (the caller opens the app). */
    fun open(preview: NotificationPreview): ActionResult? {
        val intent = preview.contentIntent ?: return null
        val result = send(intent, fillIn = null)
        if (result == ActionResult.Done && preview.autoCancel) previews.cancel(preview.key)
        return result
    }

    fun dismiss(preview: NotificationPreview): ActionResult =
        if (previews.cancel(preview.key)) ActionResult.Done else ActionResult.Failed(R.string.error_notification_gone)

    /** Fills the reply's free-form input with [text] and fires the action, as the shade's reply field does. */
    fun reply(preview: NotificationPreview, text: String): ActionResult {
        val reply = preview.reply ?: return ActionResult.Failed(R.string.error_notification_gone)
        val fillIn = Intent()
        val results = Bundle().apply { putCharSequence(reply.resultKey, text) }
        RemoteInput.addResultsToIntent(reply.remoteInputs.toTypedArray(), fillIn, results)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            RemoteInput.setResultsSource(fillIn, RemoteInput.SOURCE_FREE_FORM_INPUT)
        }
        return send(reply.intent, fillIn)
    }

    private fun send(intent: PendingIntent, fillIn: Intent?): ActionResult = try {
        intent.send(context, 0, fillIn, null, null, null, startOptions())
        ActionResult.Done
    } catch (_: PendingIntent.CanceledException) {
        ActionResult.Failed(R.string.error_notification_gone)
    }

    /**
     * Android 14+ only lets a sent intent start an activity from the background with the
     * sender's consent; home is in front when the user taps, so it lends its own.
     */
    private fun startOptions(): Bundle? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA -> ActivityOptions.makeBasic()
            .setPendingIntentBackgroundActivityStartMode(MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE)
            .toBundle()
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> ActivityOptions.makeBasic()
            .setPendingIntentBackgroundActivityStartMode(legacyAllowedMode())
            .toBundle()
        else -> null
    }

    @Suppress("DEPRECATION") // Replaced by ALLOW_IF_VISIBLE, which only exists from Android 16.
    private fun legacyAllowedMode(): Int = ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
}
