package com.wander.mono.core.quiet

import com.wander.mono.data.QuietApps
import com.wander.mono.data.settings.QuietSettings
import com.wander.mono.data.settings.SettingsStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

/**
 * Quiet mode, whichever way it is switched (home options, settings, the Quick Settings tile):
 * every switch goes through [set], so the linked Do Not Disturb mode follows.
 */
@Singleton
class QuietMode @Inject constructor(
    private val settings: SettingsStore,
    quietApps: QuietApps,
    private val zenRule: QuietZenRule,
) {
    val enabled: Flow<Boolean> = settings[QuietSettings.Enabled]

    val state: Flow<QuietState> = combine(enabled, quietApps.apps, ::QuietState)

    val linksDoNotDisturb: Flow<Boolean> = settings[QuietSettings.LinkDoNotDisturb]

    suspend fun set(on: Boolean) {
        settings.set(QuietSettings.Enabled, on)
        // A timed end belongs to the session it was set for, never to the next one.
        if (!on) settings.set(QuietSettings.Until, QuietSettings.Until.default)
        if (linksDoNotDisturb.first()) zenRule.setActive(on)
    }

    suspend fun toggle(): Boolean {
        val on = !enabled.first()
        set(on)
        return on
    }

    /** Linking while quiet mode is on starts Do Not Disturb now; unlinking ends Mono's mode. */
    suspend fun setLinksDoNotDisturb(link: Boolean) {
        settings.set(QuietSettings.LinkDoNotDisturb, link)
        zenRule.setActive(link && enabled.first())
    }
}
