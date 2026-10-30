package com.wander.mono.ui.drawer

import com.wander.mono.R
import com.wander.mono.core.contacts.ContactSearch
import com.wander.mono.core.siblings.WandaSearch
import com.wander.mono.core.system.Clipboard
import com.wander.mono.core.system.SettingsPages
import com.wander.mono.core.system.WebSearch
import com.wander.mono.ui.UserMessages
import javax.inject.Inject

/**
 * What tapping a result of [SearchSources] does: a sum is copied; a contact, a settings page, a
 * song or a web search opens. Failures are reported here; the [HitOutcome] says whether the drawer
 * may close.
 */
class SourceOpener @Inject constructor(
    private val contactSearch: ContactSearch,
    private val wandaSearch: WandaSearch,
    private val settingsPages: SettingsPages,
    private val webSearch: WebSearch,
    private val clipboard: Clipboard,
    private val messages: UserMessages,
) {
    fun copy(hit: SearchHit.Calculation): HitOutcome {
        if (clipboard.copy(R.string.search_calculator_clip, hit.result)) messages.show(R.string.search_copied)
        return HitOutcome.Stayed
    }

    fun open(hit: SearchHit.Person): HitOutcome = opened(messages.report(contactSearch.open(hit.contact)))

    fun open(hit: SearchHit.Setting): HitOutcome = when (hit.page.action) {
        null -> HitOutcome.OpenMonoSettings
        else -> opened(messages.report(settingsPages.open(hit.page)))
    }

    fun open(hit: SearchHit.Track): HitOutcome = opened(messages.report(wandaSearch.open(hit.track)))

    fun open(hit: SearchHit.Web): HitOutcome = opened(messages.report(webSearch.search(hit.query)))

    private fun opened(done: Boolean) = if (done) HitOutcome.Opened else HitOutcome.Stayed
}
