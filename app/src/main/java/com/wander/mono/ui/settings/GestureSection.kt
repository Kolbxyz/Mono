package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.settings.GestureSettings
import com.wander.mono.ui.GestureViewModel

/**
 * Swipe down for notifications and double tap to lock. Both go through Mono's gesture service;
 * until the user switches it on in Android's settings, a row says so and opens that page.
 */
@Composable
fun GestureSection(settings: SettingsViewModel, gestures: GestureViewModel = viewModel()) {
    val available by gestures.available.collectAsStateWithLifecycle()
    SettingsGroup(R.string.gesture_settings_title) {
        SwitchSetting(
            settings,
            GestureSettings.SwipeDownNotifications,
            title = R.string.gesture_swipe_down,
            summary = R.string.gesture_swipe_down_summary,
        )
        SwitchSetting(
            settings,
            GestureSettings.DoubleTapLock,
            title = R.string.gesture_double_tap_lock,
            summary = R.string.gesture_double_tap_lock_summary,
        )
        if (!available) {
            ActionRow(
                title = R.string.gesture_service_grant,
                summary = R.string.gesture_service_grant_summary,
                onClick = gestures::openAccessSettings,
            )
        }
    }
}
