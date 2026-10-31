package com.wander.mono.data.settings

import com.wander.mono.core.system.BundledWallpaper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** The clock part of a [PresetLook]. */
data class PresetClock(val font: ClockFont, val weight: Int, val style: ClockStyle)

/** What a [ThemePreset] sets, and how the current settings read back for comparing with one. */
data class PresetLook(
    val palette: ThemePalette,
    val clock: PresetClock,
    val iconShape: IconShape,
    val iconMode: IconMode,
    val drawer: DrawerBackground,
    val textShadow: Boolean,
    val density: RowDensity,
)

/**
 * A whole look in one tap: palette, clock face, icons, drawer and rows together. [wallpaper] is
 * the bundled wallpaper made for it, offered after applying and never set unasked.
 */
enum class ThemePreset(val look: PresetLook, val wallpaper: BundledWallpaper?) {
    /** Mono's own defaults: the wallpaper's colours and the original face, keeping the user's wallpaper. */
    Classic(
        PresetLook(
            palette = ThemePalette.Wallpaper,
            clock = PresetClock(font = ClockFont.Flex, weight = 800, style = ClockStyle.Default),
            iconShape = IconShape.System,
            iconMode = IconMode.Colour,
            drawer = DrawerBackground.SoftBlur,
            textShadow = false,
            density = RowDensity.Default,
        ),
        wallpaper = null,
    ),
    Minimal(
        PresetLook(
            palette = ThemePalette.Sage,
            clock = PresetClock(font = ClockFont.Flex, weight = 300, style = ClockStyle.Default),
            iconShape = IconShape.Circle,
            iconMode = IconMode.TextOnly,
            drawer = DrawerBackground.SoftBlur,
            textShadow = false,
            density = RowDensity.Default,
        ),
        wallpaper = BundledWallpaper.Sage,
    ),
    Soft(
        PresetLook(
            palette = ThemePalette.Sand,
            clock = PresetClock(font = ClockFont.Rounded, weight = 600, style = ClockStyle.Default),
            iconShape = IconShape.Squircle,
            iconMode = IconMode.Themed,
            drawer = DrawerBackground.SoftBlur,
            textShadow = false,
            density = RowDensity.Comfortable,
        ),
        wallpaper = BundledWallpaper.Dune,
    ),
    Calm(
        PresetLook(
            palette = ThemePalette.Ocean,
            clock = PresetClock(font = ClockFont.Serif, weight = 400, style = ClockStyle.Default),
            iconShape = IconShape.Squircle,
            iconMode = IconMode.Colour,
            drawer = DrawerBackground.SoftBlur,
            textShadow = false,
            density = RowDensity.Default,
        ),
        wallpaper = BundledWallpaper.Mist,
    ),
    Bold(
        PresetLook(
            palette = ThemePalette.Ember,
            clock = PresetClock(font = ClockFont.Condensed, weight = 700, style = ClockStyle.Stacked),
            iconShape = IconShape.Cookie,
            iconMode = IconMode.Colour,
            drawer = DrawerBackground.Solid,
            textShadow = true,
            density = RowDensity.Default,
        ),
        wallpaper = BundledWallpaper.Ember,
    ),
    Mono(
        PresetLook(
            palette = ThemePalette.Graphite,
            clock = PresetClock(font = ClockFont.Mono, weight = 500, style = ClockStyle.Small),
            iconShape = IconShape.Circle,
            iconMode = IconMode.Themed,
            drawer = DrawerBackground.Scrim,
            textShadow = true,
            density = RowDensity.Compact,
        ),
        wallpaper = BundledWallpaper.Graphite,
    ),
}

/**
 * The writes, for one edit, that put on [look]. With [keepDensity] (the large, simple preset is
 * on, and owns row density) the density is left alone.
 */
fun presetWrites(look: PresetLook, keepDensity: Boolean): List<SettingValue<*>> = listOfNotNull(
    SettingValue(ThemeSettings.Palette, look.palette),
    SettingValue(ClockSettings.Font, look.clock.font),
    SettingValue(ClockSettings.Weight, look.clock.weight),
    SettingValue(ClockSettings.Style, look.clock.style),
    SettingValue(IconSettings.Shape, look.iconShape),
    SettingValue(IconSettings.Mode, look.iconMode),
    SettingValue(DrawerSettings.Background, look.drawer),
    SettingValue(HomeLookSettings.TextShadow, look.textShadow),
    if (keepDensity) null else SettingValue(HomeLookSettings.Density, look.density),
)

/** The settings a preset sets, as they are now, so the matching preset (if any) shows as chosen. */
fun SettingsStore.presetLook(): Flow<PresetLook> = combine(
    this[ThemeSettings.Palette],
    combine(this[ClockSettings.Font], this[ClockSettings.Weight], this[ClockSettings.Style], ::PresetClock),
    combine(this[IconSettings.Shape], this[IconSettings.Mode], ::Pair),
    this[DrawerSettings.Background],
    combine(this[HomeLookSettings.TextShadow], this[HomeLookSettings.Density], ::Pair),
) { palette, clock, (shape, mode), drawer, (shadow, density) ->
    PresetLook(palette, clock, shape, mode, drawer, shadow, density)
}
