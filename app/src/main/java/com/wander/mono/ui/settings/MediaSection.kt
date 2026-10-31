package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.MediaSettings
import com.wander.mono.data.settings.PausedHide

/** The player under the clock: whether it shows, then how it opens, its swipe and how long it stays paused. */
@Composable
fun MediaSection(settings: SettingsViewModel) {
    val shown = settings.collect(MediaSettings.ShowPlayer)
    SettingsGroup(R.string.settings_media) {
        SwitchSetting(
            settings,
            MediaSettings.ShowPlayer,
            title = R.string.settings_media_show,
            icon = R.drawable.sym_music_note,
        )
        if (shown == true) {
            SwitchSetting(
                settings,
                MediaSettings.ExpandedByDefault,
                title = R.string.settings_media_expanded,
                icon = R.drawable.sym_height,
            )
            SwitchSetting(
                settings,
                MediaSettings.SwipeToSkip,
                title = R.string.settings_media_swipe,
                icon = R.drawable.sym_swipe,
            )
            ChoiceSetting(
                settings,
                MediaSettings.PausedHideAfter,
                R.string.settings_media_paused,
                R.drawable.sym_pause_filled,
            ) { after ->
                when (after) {
                    PausedHide.FiveMinutes -> R.string.settings_media_paused_5
                    PausedHide.TenMinutes -> R.string.settings_media_paused_10
                    PausedHide.ThirtyMinutes -> R.string.settings_media_paused_30
                    PausedHide.Never -> R.string.settings_media_paused_never
                }
            }
        }
    }
}
