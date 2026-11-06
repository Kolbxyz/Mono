package com.wander.mono.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.data.settings.MediaSettings
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.home.media.MediaControls
import com.wander.mono.ui.home.media.PlayerCollapseEffects
import com.wander.mono.ui.home.media.PlayerExpansion
import com.wander.mono.ui.home.media.rememberPlayerExpansion
import com.wander.mono.ui.settings.SettingsViewModel
import com.wander.mono.ui.settings.collect

/**
 * The player's place under the clock: it grows in when something starts playing and folds away,
 * still showing what played, when it stops ([player] null). [compact] is the header's.
 *
 * The header is measured before home's list and button, so a tall player would take their
 * room. The slot sees the height left under the clock and hands the player only [playerRoom] of
 * it: with less than a roomy player's worth, the player goes compact (a smaller cover, no next
 * line), and whatever still does not fit is the player's to give up, never the button's.
 */
@Composable
fun HomePlayerSlot(
    player: HomePlayer?,
    expansion: PlayerExpansion,
    controls: MediaControls,
    icons: AppIcons,
    compact: Boolean,
    onOptions: () -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    val shown = rememberLastShown(player)
    val rows = favoriteRowMetrics(LocalRowDensity.current).minHeight * KEPT_ROWS
    BoxWithConstraints {
        val bounded = constraints.hasBoundedHeight
        val room = playerRoom(maxHeight, rows)
        val tight = compact || (bounded && room - PLAYER_GAP < ROOMY_PLAYER)
        AnimatedVisibility(
            visible = player != null,
            modifier = if (bounded) Modifier.heightIn(max = room) else Modifier,
            enter = expandVertically(motion.defaultSpatialSpec(), expandFrom = Alignment.Top) +
                fadeIn(motion.defaultEffectsSpec()),
            exit = shrinkVertically(motion.defaultSpatialSpec(), shrinkTowards = Alignment.Top) +
                fadeOut(motion.fastEffectsSpec()),
        ) {
            shown?.let {
                NowPlayingCard(it, expansion, controls, icons, onOptions, Modifier.padding(top = PLAYER_GAP), tight)
            }
        }
    }
}

/**
 * How much of [left] (the height under the clock) the player may take. Home's button is always
 * kept; [rows] of favourites are kept too while that leaves the compact player its height, else
 * the list gives way first (it scrolls, and a scroll collapses the player). The strip always fits.
 */
private fun playerRoom(left: Dp, rows: Dp): Dp {
    val withoutButton = left - HomeButtonRoom
    return maxOf(withoutButton - rows, minOf(withoutButton, PLAYER_GAP + COMPACT_PLAYER))
        .coerceAtLeast(PLAYER_GAP + SMALLEST_PLAYER)
}

/**
 * The player's expansion for home, with what collapses it: back, a scroll of home's [list] and
 * home being [covered]. It starts as the user chose ([MediaSettings.ExpandedByDefault]).
 */
@Composable
fun rememberHomePlayerExpansion(
    list: LazyListState,
    covered: Boolean,
    settings: SettingsViewModel = viewModel(),
): PlayerExpansion {
    val expandedByDefault = settings.collect(MediaSettings.ExpandedByDefault) ?: MediaSettings.ExpandedByDefault.default
    val expansion = rememberPlayerExpansion(expandedByDefault)
    PlayerCollapseEffects(expansion, list, covered, expandedByDefault)
    return expansion
}

/** [player], or while it leaves, the last one shown, so it folds away with its content. */
@Composable
private fun rememberLastShown(player: HomePlayer?): HomePlayer? {
    val last = remember { mutableStateOf(player) }
    SideEffect { if (player != null) last.value = player }
    return player ?: last.value
}

/** Between the clock's lines and the player. */
private val PLAYER_GAP = 16.dp

/** The collapsed strip's height: even a short window keeps the strip whole. */
private val SMALLEST_PLAYER = 72.dp

/** The compact expanded player's height: small cover, seek bar and transport. */
private val COMPACT_PLAYER = 260.dp

/** Room for the full player (cover, seek bar, transport and the next line); less makes it compact. */
private val ROOMY_PLAYER = 340.dp

/** How many favourite rows stay reachable under the expanded player. */
private const val KEPT_ROWS = 3
