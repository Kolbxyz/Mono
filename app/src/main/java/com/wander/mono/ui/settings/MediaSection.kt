package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.MediaSettings
import com.wander.mono.data.settings.PausedHide

/** How the now-playing card behaves: how it opens, its swipe, its colour and how long it stays paused. */
@Composable
fun MediaSection(settings: SettingsViewModel) {
    SettingsGroup(R.string.settings_media) {
        SwitchSetting(
            settings,
            MediaSettings.ExpandedByDefault,
            title = R.string.settings_media_expanded,
            icon = R.drawable.sym_music_note,
        )
        SwitchSetting(
            settings,
            MediaSettings.SwipeToSkip,
            title = R.string.settings_media_swipe,
            icon = R.drawable.sym_swipe,
        )
        SwitchSetting(
            settings,
            MediaSettings.ArtTint,
            title = R.string.settings_media_tint,
            icon = R.drawable.sym_palette,
        )
        ChoiceSetting(
            settings,
            MediaSettings.PausedHideAfter,
            R.string.settings_media_paused,
            R.drawable.sym_schedule,
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
