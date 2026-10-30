package com.wander.mono.ui.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.data.settings.ThemePalette
import com.wander.mono.data.settings.ThemeSettings
import com.wander.mono.ui.components.PressMorph
import com.wander.mono.ui.components.shapeAt
import com.wander.mono.ui.theme.colorScheme

/**
 * The palette picker: a swatch per palette in the current light or [dark] scheme (primary over
 * its other accents). The chosen one morphs from a circle into a cookie and shows a check.
 */
@Composable
internal fun PaletteRow(settings: SettingsViewModel, dark: Boolean) {
    val chosen = settings.collect(ThemeSettings.Palette) ?: return
    Column {
        ListItem(colors = settingRowColors(), leadingContent = leadingBadge(R.drawable.sym_palette)) {
            Text(stringResource(R.string.theme_palette))
        }
        LazyRow(
            Modifier.selectableGroup(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(ThemePalette.entries) { palette ->
                PaletteChip(palette, dark, selected = palette == chosen) {
                    settings.set(ThemeSettings.Palette, palette)
                }
            }
        }
    }
}

@Composable
private fun PaletteChip(palette: ThemePalette, dark: Boolean, selected: Boolean, onClick: () -> Unit) {
    val scheme = palette.colorScheme(dark)
    val motion = MaterialTheme.motionScheme
    val progress = animateFloatAsState(if (selected) 1f else 0f, motion.defaultSpatialSpec(), label = "paletteChip")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .selectable(selected, role = Role.RadioButton, onClick = onClick)
            .padding(4.dp),
    ) {
        Box(
            Modifier
                .size(SwatchSize)
                .graphicsLayer {
                    // Read in the layer block, so the morph redraws the layer without recomposing.
                    val p = progress.value
                    shape = SwatchMorph.shapeAt { p }
                    clip = true
                },
            contentAlignment = Alignment.Center,
        ) {
            Swatch(scheme)
            androidx.compose.animation.AnimatedVisibility(
                visible = selected,
                enter = scaleIn(motion.fastSpatialSpec()) + fadeIn(motion.fastEffectsSpec()),
                exit = scaleOut(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
            ) {
                Box(Modifier.size(CheckSize).background(scheme.primary, CircleShape), Alignment.Center) {
                    Icon(painterResource(R.drawable.sym_check), contentDescription = null, tint = scheme.onPrimary)
                }
            }
        }
        Text(
            stringResource(palette.label()),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

/** Primary across the top half; the secondary container and tertiary share the bottom. */
@Composable
private fun Swatch(scheme: ColorScheme) {
    Canvas(Modifier.fillMaxSize()) {
        val half = size.height / 2
        drawRect(scheme.primary, size = Size(size.width, half))
        drawRect(scheme.secondaryContainer, topLeft = Offset(0f, half), size = Size(size.width / 2, half))
        drawRect(scheme.tertiary, topLeft = Offset(size.width / 2, half), size = Size(size.width / 2, half))
    }
}

internal fun ThemePalette.label(): Int = when (this) {
    ThemePalette.Wallpaper -> R.string.theme_palette_wallpaper
    ThemePalette.Graphite -> R.string.theme_palette_graphite
    ThemePalette.Sand -> R.string.theme_palette_sand
    ThemePalette.Sage -> R.string.theme_palette_sage
    ThemePalette.Ocean -> R.string.theme_palette_ocean
    ThemePalette.Dusk -> R.string.theme_palette_dusk
    ThemePalette.Ember -> R.string.theme_palette_ember
}

private val SwatchMorph = PressMorph.Polygons(MaterialShapes.Circle, MaterialShapes.Cookie9Sided)
private val SwatchSize = 56.dp
private val CheckSize = 28.dp
