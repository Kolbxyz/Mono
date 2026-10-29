package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import com.wander.mono.R
import com.wander.mono.data.settings.DrawerBackground
import com.wander.mono.data.settings.DrawerSettings

/** What the app drawer is drawn on: scrim, blurred wallpaper or solid. */
@Composable
fun DrawerSection(settings: SettingsViewModel) {
    SettingsGroup(R.string.drawer_settings_title) {
        ChoiceSetting(settings, DrawerSettings.Background, R.string.drawer_background) { background ->
            when (background) {
                DrawerBackground.Scrim -> R.string.drawer_background_scrim
                DrawerBackground.Blur -> R.string.drawer_background_blur
                DrawerBackground.Solid -> R.string.drawer_background_solid
            }
        }
    }
}
