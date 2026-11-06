package com.wander.mono.ui.drawer

import com.wander.mono.core.apps.AppActions
import com.wander.mono.core.contacts.ContactReach
import com.wander.mono.core.system.ClockActions
import com.wander.mono.di.AppScope
import com.wander.mono.ui.UserMessages
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * What tapping a search result (or the keyboard's Go on the top one) does, each kind to whatever
 * opens it, and remembering in [SearchMemory] what the query opened.
 */
class SearchOpener @Inject constructor(
    private val actions: AppActions,
    private val clock: ClockActions,
    private val sources: SourceOpener,
    private val handOffs: SearchHandOffs,
    private val memory: SearchMemory,
    private val messages: UserMessages,
    @AppScope private val scope: CoroutineScope,
) {
    /** Opens [hit], found for [query]. */
    fun open(hit: SearchHit, query: String): HitOutcome {
        val outcome = outcomeOf(hit)
        if (outcome == HitOutcome.Opened) {
            scope.launch { memory.remember(query, hit) }
        }
        return outcome
    }

    private fun outcomeOf(hit: SearchHit): HitOutcome = when (hit) {
        is SearchHit.App -> opened(messages.report(actions.launch(hit.entry.app.key)))
        is SearchHit.Shortcut -> opened(messages.report(actions.launchShortcut(hit.shortcut)))
        is SearchHit.Calculation -> sources.copy(hit)
        is SearchHit.Timer -> opened(messages.report(clock.setTimer(hit.seconds)))
        is SearchHit.Alarm -> opened(messages.report(clock.setAlarm(hit.time.hour, hit.time.minute)))
        is SearchHit.HandOff -> handOffs.open(hit)
        is SearchHit.Person -> sources.open(hit)
        is SearchHit.Track -> sources.open(hit)
        is SearchHit.Setting -> sources.open(hit)
        is SearchHit.Web -> sources.open(hit)
    }

    /** One of a contact's Call, Message and Email actions, remembered like opening the contact. */
    fun reach(hit: SearchHit.Person, how: ContactReach, query: String): HitOutcome {
        val outcome = handOffs.reach(hit, how)
        if (outcome == HitOutcome.Opened) scope.launch { memory.remember(query, hit) }
        return outcome
    }

    private fun opened(done: Boolean) = if (done) HitOutcome.Opened else HitOutcome.Stayed
}
