package com.wander.mono.ui.theme

/*
 * The deeper palettes: Ocean, Dusk and Ember.
 * Every role picked by hand on Material's tonal steps (light: accents at tone 40, containers at
 * 90; dark: accents at 80, containers at 30; surfaces from 98 down to 87 and from 4 up to 24), so
 * each scheme keeps Material's contrast pairs. [HandPalette] turns them into schemes.
 */

/** Ocean: deep sea blue and slate, a sea-glass teal accent. */
internal val OceanPalette = HandPalette(
    light = SchemeTones(
        primary = 0xFF06658B, onPrimary = 0xFFFFFFFF, primaryContainer = 0xFFC7E7FF,
        onPrimaryContainer = 0xFF014C6A, secondary = 0xFF446272, onSecondary = 0xFFFFFFFF,
        secondaryContainer = 0xFFC7E7F9, onSecondaryContainer = 0xFF2C4B59, tertiary = 0xFF14696B,
        onTertiary = 0xFFFFFFFF, tertiaryContainer = 0xFFA4EFF1, onTertiaryContainer = 0xFF024F51,
        surfaceDim = 0xFFD3DBE1, surface = 0xFFF4FAFE, surfaceBright = 0xFFF4FAFE,
        surfaceContainerLowest = 0xFFFFFFFF, surfaceContainerLow = 0xFFEDF5FA, surfaceContainer = 0xFFE7EFF5,
        surfaceContainerHigh = 0xFFE1E9EF, surfaceContainerHighest = 0xFFDBE4E9, onSurface = 0xFF161C20,
        onSurfaceVariant = 0xFF3A4952, outline = 0xFF6A7983, outlineVariant = 0xFFB8C9D4,
    ),
    dark = SchemeTones(
        primary = 0xFF83CFFF, onPrimary = 0xFF01344A, primaryContainer = 0xFF014C6A,
        onPrimaryContainer = 0xFFC7E7FF, secondary = 0xFFABCBDC, onSecondary = 0xFF133442,
        secondaryContainer = 0xFF2C4B59, onSecondaryContainer = 0xFFC7E7F9, tertiary = 0xFF89D3D5,
        onTertiary = 0xFF003738, tertiaryContainer = 0xFF024F51, onTertiaryContainer = 0xFFA4EFF1,
        surfaceDim = 0xFF0D1418, surface = 0xFF0D1418, surfaceBright = 0xFF333A3E,
        surfaceContainerLowest = 0xFF060F14, surfaceContainerLow = 0xFF161C20, surfaceContainer = 0xFF1A2024,
        surfaceContainerHigh = 0xFF242B2F, surfaceContainerHighest = 0xFF2F363A, onSurface = 0xFFDBE4E9,
        onSurfaceVariant = 0xFFB8C9D4, outline = 0xFF83939D, outlineVariant = 0xFF3A4952,
    ),
)

/** Dusk: plum and mauve over violet-grey surfaces, a rose accent. */
internal val DuskPalette = HandPalette(
    light = SchemeTones(
        primary = 0xFF775082, onPrimary = 0xFFFFFFFF, primaryContainer = 0xFFF8D8FF,
        onPrimaryContainer = 0xFF5E3869, secondary = 0xFF67596E, onSecondary = 0xFFFFFFFF,
        secondaryContainer = 0xFFEDDDF5, onSecondaryContainer = 0xFF4F4256, tertiary = 0xFF874F4D,
        onTertiary = 0xFFFFFFFF, tertiaryContainer = 0xFFFEDAD8, onTertiaryContainer = 0xFF6D3737,
        surfaceDim = 0xFFDDD8DF, surface = 0xFFFCF8FF, surfaceBright = 0xFFFCF8FF,
        surfaceContainerLowest = 0xFFFFFFFF, surfaceContainerLow = 0xFFF7F2F9, surfaceContainer = 0xFFF1ECF3,
        surfaceContainerHigh = 0xFFEBE6ED, surfaceContainerHighest = 0xFFE6E1E8, onSurface = 0xFF1E1A1F,
        onSurfaceVariant = 0xFF4C444F, outline = 0xFF7C7481, outlineVariant = 0xFFCCC3D1,
    ),
    dark = SchemeTones(
        primary = 0xFFE4B7EF, onPrimary = 0xFF462151, primaryContainer = 0xFF5E3869,
        onPrimaryContainer = 0xFFF8D8FF, secondary = 0xFFD1C1D9, onSecondary = 0xFF382C3F,
        secondaryContainer = 0xFF4F4256, onSecondaryContainer = 0xFFEDDDF5, tertiary = 0xFFF8B6B3,
        onTertiary = 0xFF532021, tertiaryContainer = 0xFF6D3737, onTertiaryContainer = 0xFFFEDAD8,
        surfaceDim = 0xFF161217, surface = 0xFF161217, surfaceBright = 0xFF3C383D,
        surfaceContainerLowest = 0xFF110C13, surfaceContainerLow = 0xFF1E1A1F, surfaceContainer = 0xFF221E23,
        surfaceContainerHigh = 0xFF2C292E, surfaceContainerHighest = 0xFF373339, onSurface = 0xFFE6E1E8,
        onSurfaceVariant = 0xFFCCC3D1, outline = 0xFF968E9B, outlineVariant = 0xFF4C444F,
    ),
)

/** Ember: burnt orange and smoky brown, an amber accent. */
internal val EmberPalette = HandPalette(
    light = SchemeTones(
        primary = 0xFF9C432B, onPrimary = 0xFFFFFFFF, primaryContainer = 0xFFFFDBD0,
        onPrimaryContainer = 0xFF7F2A16, secondary = 0xFF79564A, onSecondary = 0xFFFFFFFF,
        secondaryContainer = 0xFFFFDBCF, onSecondaryContainer = 0xFF5F3F34, tertiary = 0xFF755A2A,
        onTertiary = 0xFFFFFFFF, tertiaryContainer = 0xFFFFDEAE, onTertiaryContainer = 0xFF5B4213,
        surfaceDim = 0xFFE2D8D5, surface = 0xFFFFF8F6, surfaceBright = 0xFFFFF8F6,
        surfaceContainerLowest = 0xFFFFFFFF, surfaceContainerLow = 0xFFFCF1EF, surfaceContainer = 0xFFF6ECE9,
        surfaceContainerHigh = 0xFFF0E6E3, surfaceContainerHighest = 0xFFEBE0DE, onSurface = 0xFF211A18,
        onSurfaceVariant = 0xFF53433F, outline = 0xFF85736E, outlineVariant = 0xFFD6C2BD,
    ),
    dark = SchemeTones(
        primary = 0xFFFFB59F, onPrimary = 0xFF601401, primaryContainer = 0xFF7F2A16,
        onPrimaryContainer = 0xFFFFDBD0, secondary = 0xFFE7BDAF, onSecondary = 0xFF47291E,
        secondaryContainer = 0xFF5F3F34, onSecondaryContainer = 0xFFFFDBCF, tertiary = 0xFFE5C18C,
        onTertiary = 0xFF412D00, tertiaryContainer = 0xFF5B4213, onTertiaryContainer = 0xFFFFDEAE,
        surfaceDim = 0xFF19120F, surface = 0xFF19120F, surfaceBright = 0xFF3F3735,
        surfaceContainerLowest = 0xFF150C08, surfaceContainerLow = 0xFF211A18, surfaceContainer = 0xFF251E1C,
        surfaceContainerHigh = 0xFF302826, surfaceContainerHighest = 0xFF3B3331, onSurface = 0xFFEBE0DE,
        onSurfaceVariant = 0xFFD6C2BD, outline = 0xFF9F8D88, outlineVariant = 0xFF53433F,
    ),
)
