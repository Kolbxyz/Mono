package com.wander.mono.ui.theme

/*
 * The quiet palettes: Graphite, Sand and Sage.
 * Every role picked by hand on Material's tonal steps (light: accents at tone 40, containers at
 * 90; dark: accents at 80, containers at 30; surfaces from 98 down to 87 and from 4 up to 24), so
 * each scheme keeps Material's contrast pairs. [HandPalette] turns them into schemes.
 */

/** Graphite: near-neutral greys with a faint cool cast and a warm stone accent. Quiet, for the purist. */
internal val GraphitePalette = HandPalette(
    light = SchemeTones(
        primary = 0xFF585F68, onPrimary = 0xFFFFFFFF, primaryContainer = 0xFFDCE3EE,
        onPrimaryContainer = 0xFF414750, secondary = 0xFF5A5F65, onSecondary = 0xFFFFFFFF,
        secondaryContainer = 0xFFDEE3EA, onSecondaryContainer = 0xFF43474D, tertiary = 0xFF695C53,
        onTertiary = 0xFFFFFFFF, tertiaryContainer = 0xFFF0DFD5, onTertiaryContainer = 0xFF51443C,
        surfaceDim = 0xFFD8DADD, surface = 0xFFF8F9FC, surfaceBright = 0xFFF8F9FC,
        surfaceContainerLowest = 0xFFFFFFFF, surfaceContainerLow = 0xFFF2F4F6, surfaceContainer = 0xFFECEEF1,
        surfaceContainerHigh = 0xFFE6E8EB, surfaceContainerHighest = 0xFFE1E3E5, onSurface = 0xFF1A1C1D,
        onSurfaceVariant = 0xFF44474B, outline = 0xFF74777C, outlineVariant = 0xFFC3C7CC,
    ),
    dark = SchemeTones(
        primary = 0xFFC0C7D1, onPrimary = 0xFF2B3139, primaryContainer = 0xFF414750,
        onPrimaryContainer = 0xFFDCE3EE, secondary = 0xFFC2C7CE, onSecondary = 0xFF2D3136,
        secondaryContainer = 0xFF43474D, onSecondaryContainer = 0xFFDEE3EA, tertiary = 0xFFD4C3BA,
        onTertiary = 0xFF3A2E26, tertiaryContainer = 0xFF51443C, onTertiaryContainer = 0xFFF0DFD5,
        surfaceDim = 0xFF121315, surface = 0xFF121315, surfaceBright = 0xFF38393B,
        surfaceContainerLowest = 0xFF0C0E11, surfaceContainerLow = 0xFF1A1C1D, surfaceContainer = 0xFF1E2022,
        surfaceContainerHigh = 0xFF292A2C, surfaceContainerHighest = 0xFF333537, onSurface = 0xFFE1E3E5,
        onSurfaceVariant = 0xFFC3C7CC, outline = 0xFF8E9196, outlineVariant = 0xFF44474B,
    ),
)

/** Sand: warm ochre and clay over parchment surfaces, an olive accent. */
internal val SandPalette = HandPalette(
    light = SchemeTones(
        primary = 0xFF7C572D, onPrimary = 0xFFFFFFFF, primaryContainer = 0xFFFFDDBD,
        onPrimaryContainer = 0xFF623F17, secondary = 0xFF6E5B49, onSecondary = 0xFFFFFFFF,
        secondaryContainer = 0xFFF6DECA, onSecondaryContainer = 0xFF554332, tertiary = 0xFF5B623D,
        onTertiary = 0xFFFFFFFF, tertiaryContainer = 0xFFE0E6BC, onTertiaryContainer = 0xFF434A27,
        surfaceDim = 0xFFE0D9D3, surface = 0xFFFFF8F3, surfaceBright = 0xFFFFF8F3,
        surfaceContainerLowest = 0xFFFFFFFF, surfaceContainerLow = 0xFFFAF2EC, surfaceContainer = 0xFFF4EDE7,
        surfaceContainerHigh = 0xFFEEE7E1, surfaceContainerHighest = 0xFFE8E1DB, onSurface = 0xFF1F1B16,
        onSurfaceVariant = 0xFF4F453B, outline = 0xFF81756A, outlineVariant = 0xFFD2C4B9,
    ),
    dark = SchemeTones(
        primary = 0xFFEDBE8F, onPrimary = 0xFF482A00, primaryContainer = 0xFF623F17,
        onPrimaryContainer = 0xFFFFDDBD, secondary = 0xFFD9C3AE, onSecondary = 0xFF3D2D1D,
        secondaryContainer = 0xFF554332, onSecondaryContainer = 0xFFF6DECA, tertiary = 0xFFC4CAA1,
        onTertiary = 0xFF2C3312, tertiaryContainer = 0xFF434A27, onTertiaryContainer = 0xFFE0E6BC,
        surfaceDim = 0xFF18120D, surface = 0xFF18120D, surfaceBright = 0xFF3E3834,
        surfaceContainerLowest = 0xFF140D05, surfaceContainerLow = 0xFF1F1B16, surfaceContainer = 0xFF231F1A,
        surfaceContainerHigh = 0xFF2E2925, surfaceContainerHighest = 0xFF39342F, onSurface = 0xFFE8E1DB,
        onSurfaceVariant = 0xFFD2C4B9, outline = 0xFF9B8F84, outlineVariant = 0xFF4F453B,
    ),
)

/** Sage: soft herbal greens over pale linen, a cool teal accent. */
internal val SagePalette = HandPalette(
    light = SchemeTones(
        primary = 0xFF4A663F, onPrimary = 0xFFFFFFFF, primaryContainer = 0xFFCCECBF,
        onPrimaryContainer = 0xFF324E29, secondary = 0xFF57614F, onSecondary = 0xFFFFFFFF,
        secondaryContainer = 0xFFDBE6D1, onSecondaryContainer = 0xFF404A38, tertiary = 0xFF336665,
        onTertiary = 0xFFFFFFFF, tertiaryContainer = 0xFFB7EDEB, onTertiaryContainer = 0xFF194E4D,
        surfaceDim = 0xFFD8DBD5, surface = 0xFFF7FAF5, surfaceBright = 0xFFF7FAF5,
        surfaceContainerLowest = 0xFFFFFFFF, surfaceContainerLow = 0xFFF2F4EF, surfaceContainer = 0xFFECEFE9,
        surfaceContainerHigh = 0xFFE6E9E4, surfaceContainerHighest = 0xFFE0E3DE, onSurface = 0xFF1A1C18,
        onSurfaceVariant = 0xFF43483F, outline = 0xFF74796F, outlineVariant = 0xFFC3C8BE,
    ),
    dark = SchemeTones(
        primary = 0xFFB0CFA4, onPrimary = 0xFF1C3714, primaryContainer = 0xFF324E29,
        onPrimaryContainer = 0xFFCCECBF, secondary = 0xFFBFCAB5, onSecondary = 0xFF2A3323,
        secondaryContainer = 0xFF404A38, onSecondaryContainer = 0xFFDBE6D1, tertiary = 0xFF9BD0CE,
        onTertiary = 0xFF023736, tertiaryContainer = 0xFF194E4D, onTertiaryContainer = 0xFFB7EDEB,
        surfaceDim = 0xFF12140F, surface = 0xFF12140F, surfaceBright = 0xFF383A36,
        surfaceContainerLowest = 0xFF0C0F09, surfaceContainerLow = 0xFF1A1C18, surfaceContainer = 0xFF1E201C,
        surfaceContainerHigh = 0xFF282B27, surfaceContainerHighest = 0xFF333531, onSurface = 0xFFE0E3DE,
        onSurfaceVariant = 0xFFC3C8BE, outline = 0xFF8D9288, outlineVariant = 0xFF43483F,
    ),
)
