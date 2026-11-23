package com.wander.mono.core.notifications

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

/** How much of an active notification may show on home: previews and live rows read it alike. */
class NotificationRedactions @Inject constructor(
    @ApplicationContext context: Context,
    private val profiles: Profiles,
) {
    private val userManager = context.getSystemService(UserManager::class.java)

    /** From the app's visibility, the user's override and the profile's lock. [ranking] is reused scratch. */
    fun of(sbn: StatusBarNotification, rankings: RankingMap, ranking: Ranking): Redaction {
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

    /**
     * A paused work profile, or one whose storage is still locked. Only Mono's own profiles are asked:
     * a notification for every user at once, or for a user outside them, has no lock of Mono's to
     * check, and asking about it throws a [SecurityException].
     */
    private fun isLocked(user: UserHandle): Boolean =
        user != profiles.main &&
            profiles.isOwn(user) &&
            (userManager.isQuietModeEnabled(user) || !userManager.isUserUnlocked(user))
}
