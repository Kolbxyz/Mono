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
import com.wander.mono.core.security.AppLock
import com.wander.mono.core.security.LaunchRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Opening, dismissing and replying to a notification from its preview row, and opening a live row.
 * Whatever reaches into the app (its tap target, a button, a reply) passes [AppLock] first, as a
 * launch from [AppActions][com.wander.mono.core.apps.AppActions] does.
 */
@Singleton
class NotificationActions @Inject constructor(
    @ApplicationContext private val context: Context,
    private val previews: NotificationPreviews,
    private val lock: AppLock,
) {
    /** Sends the notification's own tap intent; null when it has none (the caller opens the app). */
    fun open(preview: NotificationPreview): ActionResult? {
        val intent = preview.contentIntent ?: return null
        val cancels = preview.key.takeIf { preview.autoCancel }
        return send(LaunchRequest.Notification(preview.owner, intent, cancels = cancels))
    }

    /** Opens one conversation of an expanded row; null when it has no tap intent (the caller opens the app). */
    fun open(conversation: PreviewConversation): ActionResult? {
        val intent = conversation.contentIntent ?: return null
        val cancels = conversation.key.takeIf { conversation.autoCancel }
        return send(LaunchRequest.Notification(conversation.owner, intent, cancels = cancels))
    }

    /** Sends a live row's tap intent; null when it has none (the caller opens the app). Ongoing, so never cancelled. */
    fun open(activity: LiveActivity): ActionResult? =
        activity.contentIntent?.let { send(LaunchRequest.Notification(activity.owner, it)) }

    /** One of the notification's own buttons, fired as the shade would. */
    fun fire(action: PreviewAction): ActionResult = send(LaunchRequest.Notification(action.owner, action.intent))

    /**
     * Sends [request], or holds it for the screen lock when its app is locked (which counts as done
     * here); [AppActions][com.wander.mono.core.apps.AppActions] runs it again once confirmed.
     */
    fun send(request: LaunchRequest.Notification): ActionResult {
        if (lock.holdsBack(request)) return ActionResult.Done
        val result = send(request.intent, request.fillIn)
        if (result == ActionResult.Done) request.cancels?.let(previews::cancel)
        return result
    }

    /**
     * Marks the app's notifications read: through the app's own mark-as-read action where it has
     * one, so the app knows too, else by clearing the notification. Failed when neither is possible.
     */
    fun markRead(preview: NotificationPreview): ActionResult {
        val results = preview.expansion.handles.mapNotNull { handle ->
            when {
                handle.markRead != null -> send(handle.markRead, fillIn = null)
                handle.isClearable -> if (previews.cancel(handle.key)) ActionResult.Done else GONE
                else -> null
            }
        }
        if (results.isEmpty()) return ActionResult.Failed(R.string.preview_error_mark_read)
        return results.firstOrNull { it is ActionResult.Failed } ?: ActionResult.Done
    }

    /** Snoozes the app's notifications for [MUTE_MS]; Mono has no mute of its own to add them to. */
    fun mute(preview: NotificationPreview): ActionResult {
        val handles = preview.expansion.handles
        if (handles.isEmpty()) return GONE
        return if (handles.all { previews.snooze(it.key, MUTE_MS) }) ActionResult.Done else GONE
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
        return send(LaunchRequest.Notification(preview.owner, reply.intent, fillIn))
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

    private companion object {
        val GONE = ActionResult.Failed(R.string.error_notification_gone)

        /** How long Mute snoozes for, as its label says. */
        const val MUTE_MS = 60 * 60 * 1_000L
    }
}
