package com.wander.mono.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    AnimatedVisibility(
        visible = player != null,
        enter = expandVertically(motion.defaultSpatialSpec(), expandFrom = Alignment.Top) +
            fadeIn(motion.defaultEffectsSpec()),
        exit = shrinkVertically(motion.defaultSpatialSpec(), shrinkTowards = Alignment.Top) +
            fadeOut(motion.fastEffectsSpec()),
    ) {
        shown?.let {
            NowPlayingCard(it, expansion, controls, icons, onOptions, Modifier.padding(top = PLAYER_GAP), compact)
        }
    }
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
