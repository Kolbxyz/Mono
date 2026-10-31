package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppActions
import com.wander.mono.core.contacts.ContactReach
import com.wander.mono.core.contacts.ContactReacher
import com.wander.mono.core.notifications.NotificationActions
import com.wander.mono.core.system.TextHandOff
import com.wander.mono.ui.UserMessages
import javax.inject.Inject

/**
 * What tapping a hand-off result does: a link opens, text goes to the share sheet, a note to the app
 * the user picks, a number to the dialer, a contact is called, texted or mailed by the app that does
 * so, and a notification fires its own tap intent.
 * Failures are reported here; the [HitOutcome] says whether the drawer may close.
 */
class SearchHandOffs @Inject constructor(
    private val text: TextHandOff,
    private val reacher: ContactReacher,
    private val notifications: NotificationActions,
    private val apps: AppActions,
    private val messages: UserMessages,
) {
    /** Whether an app opens the link; resolves an intent, so not on the main thread. */
    fun canOpen(hit: SearchHit.Link): Boolean = text.canOpen(hit.url)

    fun open(hit: SearchHit.Link): HitOutcome = opened(messages.report(text.open(hit.url)))

    fun share(hit: SearchHit.ShareText): HitOutcome = opened(messages.report(text.share(hit.text)))

    fun note(hit: SearchHit.Note): HitOutcome = opened(messages.report(text.note(hit.text)))

    /** Whether an app dials numbers; resolves an intent, so not on the main thread. */
    fun canDial(): Boolean = text.canDial()

    fun dial(hit: SearchHit.Dial): HitOutcome = opened(messages.report(text.dial(hit.number)))

    fun reach(hit: SearchHit.Person, how: ContactReach): HitOutcome =
        opened(messages.report(reacher.reach(hit.contact, how)))

    /**
     * Fires the conversation's (or else the notification's) tap intent, as the home preview does;
     * one that came without any opens its app instead.
     */
    fun open(hit: SearchHit.Notice): HitOutcome {
        val sent = hit.conversation?.let { notifications.open(it) } ?: notifications.open(hit.preview)
        return opened(messages.report(sent ?: apps.launch(hit.app.key)))
    }

    private fun opened(done: Boolean) = if (done) HitOutcome.Opened else HitOutcome.Stayed
}
