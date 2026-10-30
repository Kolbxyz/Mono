package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.wander.mono.R
import com.wander.mono.data.settings.DrawerBackground
import com.wander.mono.data.settings.DrawerSettings

/**
 * What the app drawer is drawn on (scrim, blurred wallpaper or solid), whether opening it brings up
 * the keyboard, and which edge its letter rail is on.
 */
@Composable
fun DrawerSection(settings: SettingsViewModel) {
    // The rail moves to the start edge: the left, or the right in a right-to-left language.
    val railTitle = when (LocalLayoutDirection.current) {
        LayoutDirection.Ltr -> R.string.drawer_rail_left
        LayoutDirection.Rtl -> R.string.drawer_rail_right
    }
    SettingsGroup(R.string.drawer_settings_title) {
        ChoiceSetting(settings, DrawerSettings.Background, R.string.drawer_background) { background ->
            when (background) {
                DrawerBackground.Scrim -> R.string.drawer_background_scrim
                DrawerBackground.Blur -> R.string.drawer_background_blur
                DrawerBackground.Solid -> R.string.drawer_background_solid
            }
        }
        SwitchSetting(
            settings,
            DrawerSettings.KeyboardOnOpen,
            title = R.string.drawer_keyboard_on_open,
            summary = R.string.drawer_keyboard_on_open_summary,
        )
        SwitchSetting(
            settings,
            DrawerSettings.RailAtStart,
            title = railTitle,
            summary = R.string.drawer_rail_side_summary,
        )
    }
}
