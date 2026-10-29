package com.wander.mono.ui.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialShapes
import androidx.compose.ui.graphics.Color
import androidx.graphics.shapes.RoundedPolygon
import com.wander.mono.R

/**
 * The settings hub's places to go, each a page of its sections ([sectionsOf]). The badge shape
 * tells the rows apart at a glance; the colour comes from the wallpaper theme, cycling through its
 * three container roles so neighbours never match.
 */
enum class SettingsCategory(
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    @DrawableRes val icon: Int,
) {
    HomeClock(R.string.settings_category_home, R.string.settings_category_home_sub, R.drawable.sym_schedule),
    DrawerSearch(R.string.settings_category_drawer, R.string.settings_category_drawer_sub, R.drawable.sym_search),
    Appearance(
        R.string.settings_category_appearance,
        R.string.settings_category_appearance_sub,
        R.drawable.sym_palette,
    ),
    Notifications(
        R.string.settings_category_notifications,
        R.string.settings_category_notifications_sub,
        R.drawable.sym_notifications,
    ),
    Gestures(R.string.settings_category_gestures, R.string.settings_category_gestures_sub, R.drawable.sym_swipe),
    PrivacyAbout(R.string.settings_category_privacy, R.string.settings_category_privacy_sub, R.drawable.sym_shield),
    ;

    /** The badge at rest; every badge relaxes into a circle under the finger. */
    val shape: RoundedPolygon
        get() = when (this) {
            HomeClock -> MaterialShapes.Clover4Leaf
            DrawerSearch -> MaterialShapes.Burst
            Appearance -> MaterialShapes.Cookie12Sided
            Notifications -> MaterialShapes.Square
            Gestures -> MaterialShapes.Cookie4Sided
            PrivacyAbout -> MaterialShapes.Pentagon
        }

    /** The badge's container and glyph colours: primary, secondary, tertiary, in turn. */
    fun colors(scheme: ColorScheme): Pair<Color, Color> = when (ordinal % THEME_ROLES) {
        0 -> scheme.primaryContainer to scheme.onPrimaryContainer
        1 -> scheme.secondaryContainer to scheme.onSecondaryContainer
        else -> scheme.tertiaryContainer to scheme.onTertiaryContainer
    }

    private companion object {
        const val THEME_ROLES = 3
    }
}
