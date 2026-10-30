package com.wander.mono.ui.home.live

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.core.notifications.LiveActivityKind
import com.wander.mono.ui.home.LiveActivityRow

/**
 * An ongoing notification inline under its app, on the same tonal card as the collapsed now-playing
 * row: a progress with its wavy line, a timer in large digits, or the next turn of a route. Tapping
 * it opens what the notification opens; only a timer offers buttons, its own.
 */
@Composable
fun LiveActivityCard(row: LiveActivityRow, modifier: Modifier = Modifier, live: LiveActivityViewModel = viewModel()) {
    val activity = row.activity
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(CARD_CORNER),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            Modifier
                .clickable(onClickLabel = stringResource(R.string.live_open)) { live.open(row) }
                .padding(CARD_INSET),
        ) {
            when (activity.kind) {
                LiveActivityKind.Progress -> LiveProgressContent(activity)
                LiveActivityKind.Timer -> LiveTimerContent(activity, onAction = live::fire)
                LiveActivityKind.Navigation -> LiveNavigationContent(activity)
            }
        }
    }
}

/** The row's kind as a symbol on a primary-container disc, the one accent a calm row carries. */
@Composable
internal fun LiveBadge(@DrawableRes icon: Int, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier.size(BADGE_SIZE).background(scheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = scheme.onPrimaryContainer,
            modifier = Modifier.size(BADGE_ICON_SIZE),
        )
    }
}

/** The collapsed now-playing card's corner and inset, so live rows read as its siblings. */
private val CARD_CORNER = 26.dp
private val CARD_INSET = 16.dp
private val BADGE_SIZE = 40.dp
private val BADGE_ICON_SIZE = 22.dp
