package com.wander.mono.core.notifications

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.service.notification.NotificationListenerService.Ranking
import android.service.notification.NotificationListenerService.RankingMap
import android.service.notification.StatusBarNotification
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.wander.mono.core.apps.Profiles
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Turns the listener's active notifications into [LiveActivities]: each is classified from a few
 * flags and extras, and only the one chosen per app and kind is read in full. Content the lockscreen
 * rules would redact never becomes a live row: a live row only shows what is really there.
 */
class LivePublisher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val live: LiveActivities,
    private val profiles: Profiles,
    private val redactions: NotificationRedactions,
) {
    /** Apps that take timers; read again each time the listener connects. */
    private var clockApps: Set<String> = emptySet()

    fun connect() {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER)
        val handlers = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentActivities(intent, 0)
        }
        clockApps = handlers.mapTo(HashSet()) { it.activityInfo.packageName }
    }

    fun publish(active: Array<StatusBarNotification>, rankings: RankingMap) {
        val ranking = Ranking()
        val candidates = active.mapNotNull { sbn ->
            val traits = traitsOf(sbn)
            val kind = liveKindOf(traits) ?: return@mapNotNull null
            if (redactions.of(sbn, rankings, ranking) != Redaction.Full) return@mapNotNull null
            LiveCandidate(sbn.key, ownerOf(sbn), kind, traits.showsChronometer, sbn.notification.`when`)
        }
        val byKey = active.associateBy { it.key }
        live.publish(
            chooseLive(candidates).mapNotNull { candidate -> byKey[candidate.key]?.let { activityOf(it, candidate) } },
        )
    }

    fun clear() = live.publish(emptyList())

    private fun traitsOf(sbn: StatusBarNotification): LiveTraits {
        val notification = sbn.notification
        val extras = notification.extras
        return LiveTraits(
            category = notification.category,
            isOngoing = sbn.isOngoing,
            isGroupSummary = notification.flags and Notification.FLAG_GROUP_SUMMARY != 0,
            isMedia = extras.containsKey(Notification.EXTRA_MEDIA_SESSION),
            showsChronometer = extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER),
            fromClockApp = sbn.packageName in clockApps,
            // Both by their documented names: the constants only exist from Android 16.
            isLiveUpdate = extras.getString(Notification.EXTRA_TEMPLATE) == PROGRESS_STYLE ||
                extras.getBoolean(EXTRA_REQUEST_PROMOTED_ONGOING),
            progress = progressIn(extras),
        )
    }

    private fun progressIn(extras: Bundle): LiveProgress? = progressOf(
        value = extras.getInt(Notification.EXTRA_PROGRESS),
        max = extras.getInt(Notification.EXTRA_PROGRESS_MAX),
        indeterminate = extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE),
    )

    /** [sbn] read in full; null when it says nothing a row could show. */
    private fun activityOf(sbn: StatusBarNotification, candidate: LiveCandidate): LiveActivity? {
        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.text(Notification.EXTRA_TITLE)
        val text = extras.text(Notification.EXTRA_TEXT) ?: extras.text(Notification.EXTRA_BIG_TEXT)
        val chronometer = if (candidate.ticking) {
            Chronometer(notification.`when`, extras.getBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN))
        } else {
            null
        }
        if (title == null && text == null && chronometer == null) return null
        return LiveActivity(
            key = sbn.key,
            owner = candidate.owner,
            kind = candidate.kind,
            title = title,
            text = text,
            progress = progressIn(extras),
            chronometer = chronometer,
            icon = if (candidate.kind == LiveActivityKind.Navigation) turnIconOf(notification) else null,
            actions = buttonsOf(notification, candidate.owner),
            contentIntent = notification.contentIntent,
        )
    }

    /**
     * The next turn: navigation apps draw it as the large icon, else it is the small icon, a mask
     * to tint. A resource icon of an app that is gone loads as null.
     */
    private fun turnIconOf(notification: Notification): LiveIcon? {
        val size = (TURN_ICON_DP * context.resources.displayMetrics.density).toInt()
        notification.getLargeIcon()?.loadDrawable(context)?.let { drawable ->
            return LiveIcon(drawable.toBitmap(size, size).asImageBitmap(), tintable = false)
        }
        return notification.smallIcon?.loadDrawable(context)?.let { drawable ->
            LiveIcon(drawable.toBitmap(size, size).asImageBitmap(), tintable = true)
        }
    }

    private fun ownerOf(sbn: StatusBarNotification) = PackageUser(sbn.packageName, profiles.serialOf(sbn.user))

    private companion object {
        /** `Notification.ProgressStyle`'s template name, as `EXTRA_TEMPLATE` holds it. */
        const val PROGRESS_STYLE = "android.app.Notification\$ProgressStyle"

        /** `Notification.EXTRA_REQUEST_PROMOTED_ONGOING`. */
        const val EXTRA_REQUEST_PROMOTED_ONGOING = "android.requestPromotedOngoing"

        /** The size the turn arrow is drawn at on home. */
        const val TURN_ICON_DP = 40
    }
}
