package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.wander.mono.R
import com.wander.mono.data.settings.DrawerBackground
import com.wander.mono.data.settings.DrawerSettings
import com.wander.mono.data.settings.DrawerSort

/**
 * What the app drawer is drawn on (scrim, blurred wallpaper or solid), how its list is ordered and
 * whether new apps gather at the top, then whether opening it brings up the keyboard and which
 * edge its letter rail is on.
 */
@Composable
fun DrawerSection(settings: SettingsViewModel) {
    // The rail moves to the start edge: the left, or the right in a right-to-left language.
    val railTitle = when (LocalLayoutDirection.current) {
        LayoutDirection.Ltr -> R.string.drawer_rail_left
        LayoutDirection.Rtl -> R.string.drawer_rail_right
    }
    SettingsGroup(R.string.drawer_settings_title) {
        ChoiceSetting(
            settings,
            DrawerSettings.Background,
            R.string.drawer_background,
            R.drawable.sym_palette,
        ) { background ->
            when (background) {
                DrawerBackground.Scrim -> R.string.drawer_background_scrim
                DrawerBackground.Blur -> R.string.drawer_background_blur
                DrawerBackground.Solid -> R.string.drawer_background_solid
            }
        }
        ChoiceSetting(settings, DrawerSettings.Sort, R.string.drawer_sort, R.drawable.sym_view_agenda) { sort ->
            when (sort) {
                DrawerSort.Alphabetical -> R.string.drawer_sort_alphabetical
                DrawerSort.Recent -> R.string.drawer_sort_recent
            }
        }
        SwitchSetting(
            settings,
            DrawerSettings.NewApps,
            title = R.string.drawer_new_apps,
            summary = R.string.drawer_new_apps_summary,
            icon = R.drawable.sym_add,
        )
        SwitchSetting(
            settings,
            DrawerSettings.KeyboardOnOpen,
            title = R.string.drawer_keyboard_on_open,
            summary = R.string.drawer_keyboard_on_open_summary,
            icon = R.drawable.sym_search,
        )
        SwitchSetting(settings, DrawerSettings.RailAtStart, title = railTitle, icon = R.drawable.sym_font_download)
    }
}
