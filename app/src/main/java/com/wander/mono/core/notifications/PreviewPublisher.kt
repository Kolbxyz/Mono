package com.wander.mono.core.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import android.service.notification.NotificationListenerService.Ranking
import android.service.notification.NotificationListenerService.RankingMap
import android.service.notification.StatusBarNotification
import com.wander.mono.core.apps.Profiles
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Turns the listener's active notifications into [NotificationPreviews]: the latest notification of
 * each app is read in full, the latest of its first few conversations for its expanded row, and the
 * rest only for their conversation counts.
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
            val redaction = redactionOf(sbn, rankings, ranking)
            owner to previewOf(sbn, owner, redaction, expansionOf(sbn, newsOf(facts, owner), byKey, rankings, ranking))
        }.toMap()
        previews.publish(latest, unreadPerConversation(facts))
    }

    /**
     * The expanded row of the app whose [latest] notification its preview shows: each conversation
     * under its own redaction, the latest notification's buttons when its content shows in full.
     */
    private fun expansionOf(
        latest: StatusBarNotification,
        news: List<NotificationFacts>,
        byKey: Map<String, StatusBarNotification>,
        rankings: RankingMap,
        ranking: Ranking,
    ): PreviewExpansion {
        val conversations = conversationKeys(news, MAX_CONVERSATIONS).mapNotNull(byKey::get).map { sbn ->
            conversationOf(sbn, redactionOf(sbn, rankings, ranking))
        }
        val full = redactionOf(latest, rankings, ranking) == Redaction.Full
        return PreviewExpansion(
            conversations = conversations,
            actions = if (full) buttonsOf(latest.notification) else emptyList(),
            handles = news.mapNotNull { byKey[it.key] }.map(::handleOf),
        )
    }

    /** How much of [sbn] may show, from the app's visibility, the user's override and the profile's lock. */
    private fun redactionOf(sbn: StatusBarNotification, rankings: RankingMap, ranking: Ranking): Redaction {
        // The user's per-app lock screen override is only readable from Android 12.
        val override = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && rankings.getRanking(sbn.key, ranking)) {
            ranking.lockscreenVisibilityOverride
        } else {
            Ranking.VISIBILITY_NO_OVERRIDE
        }
        return redactionFor(
            visibility = sbn.notification.visibility,
            userOverride = override,
            profileLocked = isLocked(sbn.user),
            hasPublicVersion = sbn.notification.publicVersion != null,
        )
    }

    fun clear() = previews.publish(emptyMap(), emptyMap())

    private fun ownerOf(sbn: StatusBarNotification) = PackageUser(sbn.packageName, profiles.serialOf(sbn.user))

    /** A paused work profile, or one whose storage is still locked. */
    private fun isLocked(user: UserHandle): Boolean =
        user != profiles.main && (userManager.isQuietModeEnabled(user) || !userManager.isUserUnlocked(user))
}
