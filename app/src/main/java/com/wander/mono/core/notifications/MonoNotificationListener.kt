package com.wander.mono.core.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.wander.mono.core.apps.Profiles
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * Bound by the system once the user grants notification access. Turns the active notifications
 * into per-app counts, previews and live rows on every post, removal and ranking change. Its binding is
 * also what lets `core/media` read the active media sessions and the preview rows dismiss.
 *
 * The callbacks arrive on the main thread; the work (a binder read of every notification, and the
 * full read of the ones shown) runs off it on one worker, in order, with a burst folded into one.
 * While nothing needs the readings ([ListenerDemand]) a change is only marked [stale] and read
 * once demand returns: a busy notification (navigation, downloads) would otherwise cost a full
 * re-read every second with the screen off.
 */
@AndroidEntryPoint
class MonoNotificationListener : NotificationListenerService() {
    @Inject
    lateinit var counts: NotificationCounts

    @Inject
    lateinit var access: ListenerAccess

    @Inject
    lateinit var profiles: Profiles

    @Inject
    lateinit var previews: PreviewPublisher

    @Inject
    lateinit var cancels: NotificationPreviews

    @Inject
    lateinit var live: LivePublisher

    @Inject
    lateinit var demand: ListenerDemand

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** One pending update at most: a later request while one runs is folded into the next. */
    private val updates = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    @Volatile
    private var connected = false

    @Volatile
    private var freshlyConnected = false

    /** A change came in that no reading has covered yet. */
    @Volatile
    private var stale = false

    override fun onCreate() {
        super.onCreate()
        scope.launch { updates.collect { update() } }
        scope.launch { demand.wanted.collect { wanted -> if (wanted && stale) updates.tryEmit(Unit) } }
    }

    override fun onListenerConnected() {
        access.recheck()
        cancels.bind(this)
        freshlyConnected = true
        connected = true
        updates.tryEmit(Unit)
    }

    override fun onListenerDisconnected() {
        access.recheck()
        cancels.bind(null)
        connected = false
        // Queued behind any update in flight, so its result cannot outlive this.
        updates.tryEmit(Unit)
    }

    override fun onDestroy() {
        cancels.bind(null)
        scope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap) = changed()

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap) = changed()

    override fun onNotificationRankingUpdate(rankingMap: RankingMap) = changed()

    /** Marked before demand is checked, so a change racing demand's return is still read. */
    private fun changed() {
        stale = true
        if (demand.wanted.value) updates.tryEmit(Unit)
    }

    private fun update() {
        stale = false
        if (freshlyConnected) {
            freshlyConnected = false
            live.connect()
        }
        // Null while the service is not (or no longer) bound.
        val active = if (connected) activeNotifications else null
        if (active == null) {
            counts.disconnected()
            previews.clear()
            live.clear()
            return
        }
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
        previews.publish(active, rankings)
        live.publish(active, rankings)
    }
}
