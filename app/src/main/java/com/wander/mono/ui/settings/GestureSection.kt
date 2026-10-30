package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.settings.GestureSettings
import com.wander.mono.data.settings.SwipeDownAction
import com.wander.mono.data.settings.SwipeUpAction
import com.wander.mono.ui.GestureViewModel

/**
 * What swiping up and down on home do, and double tap to lock. The shade, quick settings and the
 * lock go through Mono's gesture service; until the user switches it on in Android's settings, a
 * row says so and opens that page.
 */
@Composable
fun GestureSection(settings: SettingsViewModel, gestures: GestureViewModel = viewModel()) {
    val available by gestures.available.collectAsStateWithLifecycle()
    SettingsGroup(R.string.gesture_settings_title) {
        ChoiceSetting(settings, GestureSettings.SwipeUp, R.string.gesture_swipe_up) { action ->
            when (action) {
                SwipeUpAction.Drawer -> R.string.gesture_swipe_up_drawer
                SwipeUpAction.Search -> R.string.gesture_swipe_search
            }
        }
        // Four choices, one of them two words: as separate pills, so none is cut short.
        ChoiceTextSetting(settings, GestureSettings.SwipeDown, R.string.gesture_swipe_down_action, wrap = true) {
            stringResource(
                when (it) {
                    SwipeDownAction.Notifications -> R.string.gesture_swipe_down_notifications
                    SwipeDownAction.QuickSettings -> R.string.gesture_swipe_down_quick_settings
                    SwipeDownAction.Search -> R.string.gesture_swipe_search
                    SwipeDownAction.None -> R.string.gesture_swipe_down_nothing
                },
            )
        }
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
