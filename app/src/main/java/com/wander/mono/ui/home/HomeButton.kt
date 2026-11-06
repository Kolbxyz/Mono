package com.wander.mono.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.data.settings.IconMode
import com.wander.mono.ui.components.AppIcon
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.expressivePress
import com.wander.mono.ui.components.rememberMonoHaptics

/**
 * Home's round button at the bottom end, in the thumb's reach: search ([onSearch], the drawer with
 * its keyboard) or, once one is chosen, an [app] it opens ([onLaunch]). A long press chooses what it
 * opens ([onChoose]). Opaque and tonal, so it reads on any wallpaper; it gives under the finger on
 * the scheme's spring, and its glyph cross-fades when the choice changes.
 */
@Composable
fun HomeButton(
    app: LaunchableApp?,
    icons: AppIcons,
    onSearch: () -> Unit,
    onLaunch: (LaunchableApp) -> Unit,
    onChoose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val haptics = rememberMonoHaptics()
    val label = if (app != null) {
        stringResource(R.string.home_button_app, app.label)
    } else {
        stringResource(R.string.home_button_search)
    }
    val chooseLabel = stringResource(R.string.home_button_choose)
    val open = { if (app != null) onLaunch(app) else onSearch() }
    val choose = {
        haptics.longPress()
        onChoose()
    }
    Surface(
        modifier = modifier
            .size(BUTTON_SIZE)
            .expressivePress(interaction, pressedScale = PRESSED_SCALE)
            .clearAndSetSemantics {
                contentDescription = label
                onClick { open().let { true } }
                onLongClick(chooseLabel) { choose().let { true } }
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Box(
            Modifier.combinedClickable(
                interactionSource = interaction,
                indication = ripple(),
                onClick = open,
                onLongClick = choose,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Crossfade(app, animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(), label = "homeButton") {
                ButtonGlyph(it, icons)
            }
        }
    }
}

/** Search's glyph, or the chosen app's icon; its initial when icons are off. */
@Composable
private fun ButtonGlyph(app: LaunchableApp?, icons: AppIcons) {
    Box(Modifier.size(BUTTON_SIZE), contentAlignment = Alignment.Center) {
        when {
            app == null -> Icon(painterResource(R.drawable.sym_search), contentDescription = null)
            icons.mode == IconMode.TextOnly -> Text(
                app.label.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge,
            )
            else -> AppIcon(app.key, icons, size = APP_ICON_SIZE)
        }
    }
}

private val BUTTON_SIZE = 56.dp

private val APP_ICON_SIZE = 28.dp

/** How far the button gives under a press: further than a row, as it is small. */
private const val PRESSED_SCALE = 0.9f

/**
 * The height home gives the button, with the 8 dp home sets above and below it, so the player under
 * the clock and home's rail leave it this much ([HomePlayerSlot], [HomeRail]).
 */
internal val HomeButtonRoom = BUTTON_SIZE + 16.dp
