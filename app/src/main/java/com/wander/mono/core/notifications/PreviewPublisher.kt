package com.wander.mono.core.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.UserHandle
import android.os.UserManager
import android.service.notification.NotificationListenerService.Ranking
import android.service.notification.NotificationListenerService.RankingMap
import android.service.notification.StatusBarNotification
import com.wander.mono.core.apps.Profiles
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Turns the listener's active notifications into [NotificationPreviews]: only the latest
 * notification of each app is read in full, the rest only for their conversation counts.
 */
class PreviewPublisher @Inject constructor(
    @ApplicationContext context: Context,
    private val previews: NotificationPreviews,
    private val profiles: Profiles,
) {
    private val userManager = context.getSystemService(UserManager::class.java)

    fun publish(active: Array<StatusBarNotification>, rankings: RankingMap) {
        val ranking = Ranking()
        val byKey = active.associateBy { it.key }
        val facts = active.map { sbn ->
            val ranked = rankings.getRanking(sbn.key, ranking)
            NotificationFacts(
                key = sbn.key,
                owner = ownerOf(sbn),
                postTime = sbn.postTime,
                isOngoing = sbn.isOngoing,
                isGroupSummary = sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0,
                isSilent = ranked && ranking.importance < NotificationManager.IMPORTANCE_DEFAULT,
                shortcutId = sbn.notification.shortcutId,
                unread = unreadOf(sbn.notification),
            )
        }
        val latest = latestPerApp(facts).mapNotNull { (owner, key) ->
            val sbn = byKey[key] ?: return@mapNotNull null
            val override = if (rankings.getRanking(key, ranking)) {
                ranking.lockscreenVisibilityOverride
            } else {
                Ranking.VISIBILITY_NO_OVERRIDE
            }
            val redaction = redactionFor(
                visibility = sbn.notification.visibility,
                userOverride = override,
                profileLocked = isLocked(sbn.user),
                hasPublicVersion = sbn.notification.publicVersion != null,
            )
            owner to previewOf(sbn, owner, redaction)
        }.toMap()
        previews.publish(latest, unreadPerConversation(facts))
    }

    fun clear() = previews.publish(emptyMap(), emptyMap())

    private fun ownerOf(sbn: StatusBarNotification) = PackageUser(sbn.packageName, profiles.serialOf(sbn.user))

    /** A paused work profile, or one whose storage is still locked. */
    private fun isLocked(user: UserHandle): Boolean =
        user != profiles.main && (userManager.isQuietModeEnabled(user) || !userManager.isUserUnlocked(user))
}
