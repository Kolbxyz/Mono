package com.wander.mono.core.system

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Bound by Android once the user switches it on in accessibility settings. It receives no events
 * and cannot read any window (`res/xml/gesture_service.xml`): it only lends [GestureActions] the
 * global actions that lock the screen and open the notification shade.
 */
@AndroidEntryPoint
class GestureService : AccessibilityService() {
    @Inject
    lateinit var actions: GestureActions

    override fun onServiceConnected() {
        actions.bind(::performGlobalAction)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        actions.bind(null)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        actions.bind(null)
        super.onDestroy()
    }

    // No event types are requested, so neither of these is ever called with anything to handle.
    override fun onAccessibilityEvent(event: AccessibilityEvent) = Unit

    override fun onInterrupt() = Unit
}
