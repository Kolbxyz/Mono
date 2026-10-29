package com.wander.mono.core.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.wander.mono.core.apps.Profiles
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Bound by the system once the user grants notification access. Turns the active notifications
 * into per-app counts on every post, removal and ranking change. Its binding is also what lets
 * `core/media` read the active media sessions.
 */
@AndroidEntryPoint
class MonoNotificationListener : NotificationListenerService() {
    @Inject
    lateinit var counts: NotificationCounts

    @Inject
    lateinit var access: ListenerAccess

    @Inject
    lateinit var profiles: Profiles

    override fun onListenerConnected() {
        access.recheck()
        recount()
    }

    override fun onListenerDisconnected() {
        access.recheck()
        counts.publish(emptyMap())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap) = recount()

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap) = recount()

    override fun onNotificationRankingUpdate(rankingMap: RankingMap) = recount()

    private fun recount() {
        // Null while the service is not (or no longer) bound; onListenerDisconnected cleared the counts.
        val active = activeNotifications ?: return
        val rankings = currentRanking
        val ranking = Ranking()
        counts.publish(
            countBadges(
                active.map { sbn ->
                    BadgeCandidate(
                        owner = PackageUser(sbn.packageName, profiles.serialOf(sbn.user)),
                        isOngoing = sbn.isOngoing,
                        isGroupSummary = sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0,
                        canShowBadge = rankings.getRanking(sbn.key, ranking) && ranking.canShowBadge(),
                        number = sbn.notification.number,
                    )
                }
            )
        )
    }
}
