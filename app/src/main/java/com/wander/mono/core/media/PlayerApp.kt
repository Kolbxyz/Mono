package com.wander.mono.core.media

import com.wander.mono.R
import com.wander.mono.core.apps.ActionResult
import com.wander.mono.core.apps.AppActions
import com.wander.mono.core.apps.AppKey
import com.wander.mono.core.apps.Profiles
import com.wander.mono.core.notifications.NotificationActions
import com.wander.mono.core.notifications.PackageUser
import com.wander.mono.core.security.LaunchRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Opens the app behind the playing session, through the app lock like any launch: at the screen
 * its session names (its player) when it names one, else as home launches [app]. Sessions are only
 * readable for Mono's own profile, so that is the profile the screen opens in.
 */
@Singleton
class PlayerApp @Inject constructor(
    private val media: MediaSessions,
    private val notifications: NotificationActions,
    private val actions: AppActions,
    private val profiles: Profiles,
) {
    fun open(packageName: String, app: AppKey?): ActionResult {
        val screen = media.sessionActivity()
        val owner = PackageUser(packageName, profiles.serialOf(profiles.main))
        val sent = screen?.let { notifications.send(LaunchRequest.Notification(owner, it)) }
        return when {
            sent == ActionResult.Done -> ActionResult.Done
            app != null -> actions.launch(app)
            else -> sent ?: ActionResult.Failed(R.string.error_app_missing)
        }
    }
}
