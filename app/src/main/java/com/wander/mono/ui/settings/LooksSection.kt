package com.wander.mono.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.settings.ThemePreset
import com.wander.mono.data.settings.ThemeSettings
import com.wander.mono.ui.components.PressMorph
import com.wander.mono.ui.components.expressivePress
import com.wander.mono.ui.components.instantPress
import com.wander.mono.ui.theme.colorScheme
import com.wander.mono.ui.theme.isDark

/**
 * Whole looks in one tap, as a row of cards: each a miniature home in its palette, clock face and
 * icons, over the wallpaper made for it. The card matching the current settings shows as chosen;
 * after applying, the snackbar offers that wallpaper.
 */
@Composable
fun LooksSection(settings: SettingsViewModel, looks: LooksViewModel = viewModel()) {
    val current by looks.current.collectAsStateWithLifecycle()
    val dark = settings.collect(ThemeSettings.Mode)?.isDark() ?: return
    SettingsGroup(R.string.theme_presets) {
        LazyRow(
            Modifier.selectableGroup(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(ThemePreset.entries) { preset ->
                PresetCard(preset, dark, selected = preset.look == current) { looks.apply(preset) }
            }
        }
    }
}

@Composable
private fun PresetCard(preset: ThemePreset, dark: Boolean, selected: Boolean, onClick: () -> Unit) {
    val scheme = preset.look.palette.colorScheme(dark)
    val interaction = remember { MutableInteractionSource() }
    val motion = MaterialTheme.motionScheme
    val ring by animateFloatAsState(if (selected) 1f else 0f, motion.defaultEffectsSpec(), label = "presetRing")
    val shape = MaterialTheme.shapes.large
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .expressivePress(interaction, PressMorph.squarer(CardCorner))
            .instantPress(interaction)
            .selectable(selected, interaction, ripple(), role = Role.RadioButton, onClick = onClick),
    ) {
        Box(
            Modifier
                .size(CardWidth, CardHeight)
                .clip(shape)
                .background(scheme.surfaceContainer)
                .border(RingWidth, MaterialTheme.colorScheme.primary.copy(alpha = ring), shape),
        ) {
            // Under the surface scrim home draws (none with the text shadow), so the mock reads as home would.
            preset.wallpaper?.let { wallpaper ->
                val scrim = scheme.surface.copy(alpha = SCRIM_ALPHA).takeUnless { preset.look.textShadow }
                WallpaperThumbnail(wallpaper.image, CardWidth, Modifier.fillMaxSize(), scrim)
            }
            PresetHomeMock(preset.look, scheme, Modifier.fillMaxSize().padding(14.dp))
            AnimatedVisibility(
                visible = selected,
                enter = scaleIn(motion.fastSpatialSpec()) + fadeIn(motion.fastEffectsSpec()),
                exit = scaleOut(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
                modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
            ) {
                Box(
                    Modifier.size(CheckSize).background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(R.drawable.sym_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(CheckGlyph),
                    )
                }
            }
        }
        Text(
            stringResource(preset.label()),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }
}

private fun ThemePreset.label(): Int = when (this) {
    ThemePreset.Classic -> R.string.theme_preset_classic
    ThemePreset.Minimal -> R.string.theme_preset_minimal
    ThemePreset.Soft -> R.string.theme_preset_soft
    ThemePreset.Calm -> R.string.theme_preset_calm
    ThemePreset.Bold -> R.string.theme_preset_bold
    ThemePreset.Mono -> R.string.theme_preset_mono
}

private val CardWidth = 120.dp
private val CardHeight = 200.dp
private val CardCorner = 26.dp
private val RingWidth = 3.dp
private val CheckSize = 28.dp
private val CheckGlyph = 18.dp

/** Home's scrim over the wallpaper, a little lighter at this size so the wallpaper still shows. */
private const val SCRIM_ALPHA = 0.45f
