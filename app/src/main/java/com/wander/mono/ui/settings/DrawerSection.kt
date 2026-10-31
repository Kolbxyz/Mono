package com.wander.mono.ui.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.wander.mono.R
import com.wander.mono.data.settings.DrawerBackground
import com.wander.mono.data.settings.DrawerSettings

/**
 * What the app drawer is drawn on (bare or blurred wallpaper, tint or solid), whether new apps
 * gather at the top and which edge its letter rail is on. Its order is switched from the drawer's
 * own toolbar, where its effect shows.
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
            preview = { BackgroundPreview(it) },
        ) { background ->
            when (background) {
                DrawerBackground.None -> R.string.drawer_background_none
                DrawerBackground.SoftBlur -> R.string.drawer_background_soft_blur
                DrawerBackground.Scrim -> R.string.drawer_background_scrim
                DrawerBackground.Solid -> R.string.drawer_background_solid
            }
        }
        SwitchSetting(settings, DrawerSettings.NewApps, title = R.string.drawer_new_apps, icon = R.drawable.sym_add)
        SwitchSetting(settings, DrawerSettings.RailAtStart, title = railTitle, icon = R.drawable.sym_font_download)
    }
}

/**
 * Each background as a swatch of how much of the surface it lays over the wallpaper: none, a light
 * tint (the blur), the home scrim, all of it.
 */
@Composable
private fun BackgroundPreview(background: DrawerBackground) {
    val alpha = when (background) {
        DrawerBackground.None -> 0f
        DrawerBackground.SoftBlur -> BLUR_SWATCH_ALPHA
        DrawerBackground.Scrim -> SCRIM_SWATCH_ALPHA
        DrawerBackground.Solid -> 1f
    }
    ChoiceSwatchPreview(MaterialTheme.colorScheme.surface.copy(alpha = alpha))
}

private const val BLUR_SWATCH_ALPHA = 0.3f
private const val SCRIM_SWATCH_ALPHA = 0.65f
