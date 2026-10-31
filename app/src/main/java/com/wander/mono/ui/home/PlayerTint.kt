package com.wander.mono.ui.home

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.ThemeViewModel
import com.wander.mono.ui.theme.isDark

/**
 * The artwork colour (ARGB) home's scrim leans toward while the player under the clock is playing;
 * null when paused or stopped, with no artwork, with high contrast (nothing may soften the scrim's
 * job), or over "solid black wallpaper", where those pixels stay off. Both view models are the
 * activity's own, the ones home and the theme already read.
 */
@Composable
internal fun playerTint(
    highContrast: Boolean,
    home: HomeViewModel = viewModel(),
    themes: ThemeViewModel = viewModel(),
): Int? {
    val player = home.player.collectAsStateWithLifecycle().value
    val theme = themes.theme.collectAsStateWithLifecycle().value
    val dark = theme?.mode?.isDark() == true
    // An unread theme leaves it untinted rather than guess.
    val untinted = theme == null || dark && theme.amoledBlack && theme.blackWallpaper
    return player?.media?.takeIf { it.isPlaying && !highContrast && !untinted }?.artColor
}

/**
 * The scrim's colour with [art] mixed in at [PLAYER_TINT] over the opaque [surface]: the scrim keeps
 * its tone and only leans toward the artwork. Its alpha is then measured afresh ([scrimAlpha]), so
 * text keeps 4.5:1 over the tinted scrim too.
 */
internal fun tintedSurface(surface: Int, art: Int?): Int = if (art == null) surface else blend(art, surface, PLAYER_TINT)

/** How far the scrim leans toward the artwork: a wash, not a colour change. */
private const val PLAYER_TINT = 0.18f
