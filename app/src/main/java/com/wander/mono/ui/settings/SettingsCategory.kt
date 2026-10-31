package com.wander.mono.ui.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialShapes
import androidx.compose.ui.graphics.Color
import androidx.graphics.shapes.RoundedPolygon
import com.wander.mono.R

/**
 * The settings hub's places to go, each a page of its sections ([sectionsOf]), most visited first:
 * home (its clock, player, notifications and gestures) and how Mono looks, then the drawer, then
 * the quieter pages (focus, privacy, about). The badge shape tells the rows apart at a glance; the colour comes from
 * the wallpaper theme, cycling through its three container roles so neighbours never match.
 */
enum class SettingsCategory(
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    @DrawableRes val icon: Int,
) {
    Home(R.string.settings_category_home, R.string.settings_category_home_sub, R.drawable.sym_schedule),
    Appearance(
        R.string.settings_category_appearance,
        R.string.settings_category_appearance_sub,
        R.drawable.sym_palette,
    ),
    DrawerSearch(R.string.settings_category_drawer, R.string.settings_category_drawer_sub, R.drawable.sym_search),
    Focus(R.string.settings_category_focus, R.string.settings_category_focus_sub, R.drawable.sym_quiet),
    Privacy(R.string.settings_category_privacy, R.string.settings_category_privacy_sub, R.drawable.sym_shield),
    About(R.string.settings_about, R.string.settings_category_about_sub, R.drawable.sym_info),
    ;

    /** The badge at rest; every badge relaxes into a circle under the finger. */
    val shape: RoundedPolygon
        get() = when (this) {
            Home -> MaterialShapes.Clover4Leaf
            Appearance -> MaterialShapes.Cookie12Sided
            DrawerSearch -> MaterialShapes.Burst
            Focus -> MaterialShapes.Sunny
            Privacy -> MaterialShapes.Pentagon
            About -> MaterialShapes.Gem
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
