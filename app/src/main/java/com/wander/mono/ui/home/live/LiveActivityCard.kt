package com.wander.mono.ui.home.live

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.core.notifications.LiveActivityKind
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.components.PressMorph
import com.wander.mono.ui.components.expressivePress
import com.wander.mono.ui.components.rememberRowAccent
import com.wander.mono.ui.home.HomeCardCorner
import com.wander.mono.ui.home.HomeCardInset
import com.wander.mono.ui.home.HomeCardMargin
import com.wander.mono.ui.home.LiveActivityRow
import com.wander.mono.ui.home.LocalRowDensity
import com.wander.mono.ui.home.favoriteRowMetrics

/**
 * An ongoing notification inline under its app, on the same tonal card home's other rows use: a
 * progress with its wavy line, a timer in large digits, or the next turn of a route. Tapping
 * it opens what the notification opens; only a timer offers buttons, its own. With "Tint rows with
 * app colour" its badge and press take the app's colour, softly.
 */
@Composable
fun LiveActivityCard(
    row: LiveActivityRow,
    icons: AppIcons,
    modifier: Modifier = Modifier,
    live: LiveActivityViewModel = viewModel(),
) {
    val activity = row.activity
    val interaction = remember { MutableInteractionSource() }
    // The size the app's favourite row draws its icon at, so the colour comes from that cached icon.
    val accent = rememberRowAccent(row.app.key, icons, favoriteRowMetrics(LocalRowDensity.current).iconSize)
    val tone = liveTone(accent)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = HomeCardMargin)
            .expressivePress(interaction, PressMorph.squarer(HomeCardCorner)),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            Modifier
                .clickable(
                    interactionSource = interaction,
                    indication = ripple(color = accent ?: Color.Unspecified),
                    onClickLabel = stringResource(R.string.live_open),
                ) { live.open(row) }
                .padding(HomeCardInset),
        ) {
            when (activity.kind) {
                LiveActivityKind.Progress -> LiveProgressContent(activity, tone)
                LiveActivityKind.Timer -> LiveTimerContent(activity, tone, onAction = live::fire)
                LiveActivityKind.Navigation -> LiveNavigationContent(activity, tone)
            }
        }
    }
}

/** The colours of a live row's one accent, its badge or turn tile: a container and what sits on it. */
@Immutable
internal class LiveTone(val container: Color, val content: Color)

/**
 * The theme's primary container, or with an app [accent] a light veil of it holding a glyph in it,
 * so a tinted row stays as calm as an untinted one.
 */
@Composable
private fun liveTone(accent: Color?): LiveTone {
    val scheme = MaterialTheme.colorScheme
    return if (accent != null) {
        LiveTone(accent.copy(alpha = ACCENT_VEIL), accent)
    } else {
        LiveTone(scheme.primaryContainer, scheme.onPrimaryContainer)
    }
}

/**
 * The row's kind as a symbol on a [tone] cookie, the one accent a calm row carries: Material's
 * expressive shape marks what is live apart from the round icons and pills around it.
 */
@Composable
internal fun LiveBadge(@DrawableRes icon: Int, tone: LiveTone, modifier: Modifier = Modifier) {
    Box(
        modifier.size(BADGE_SIZE).background(tone.container, MaterialShapes.Cookie9Sided.toShape()),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = tone.content,
            modifier = Modifier.size(BADGE_ICON_SIZE),
        )
    }
}

/** How strongly an app's accent shows behind its badge: a tint, not a fill. */
private const val ACCENT_VEIL = 0.16f
private val BADGE_SIZE = 40.dp
private val BADGE_ICON_SIZE = 22.dp
