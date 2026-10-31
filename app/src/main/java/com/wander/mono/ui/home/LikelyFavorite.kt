package com.wander.mono.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import com.wander.mono.core.apps.AppKey
import java.time.Instant
import java.time.ZoneId

/** The hour of the day (0 to 23) at [millis], on the device's clock. */
internal fun hourOf(millis: Long): Int = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).hour

/**
 * The favourite most likely wanted at [hour], from the [counts] of home launches per hour: this
 * hour counts double, its neighbours once, so a habit at 8:55 still shows at 9:05. Only a clear
 * habit counts: enough launches, well ahead of the next favourite. Null otherwise.
 */
internal fun likelyFavorite(counts: Map<Int, Map<AppKey, Int>>, hour: Int, favorites: List<AppKey>): AppKey? {
    val around = listOf(hour to 2, (hour + HOURS - 1) % HOURS to 1, (hour + 1) % HOURS to 1)
    val scores = favorites.distinct().map { key ->
        key to around.sumOf { (h, weight) -> (counts[h]?.get(key) ?: 0) * weight }
    }.sortedByDescending { it.second }
    val best = scores.firstOrNull() ?: return null
    val runnerUp = scores.getOrNull(1)?.second ?: 0
    val clear = best.second >= MIN_SCORE && best.second * LEAD_DENOMINATOR >= runnerUp * LEAD_NUMERATOR
    return best.first.takeIf { clear }
}

/**
 * A soft tonal pill behind the favourite [likely] now: there, never moving it, so nothing shifts
 * under a thumb. It fades in and out on the effects spec.
 */
@Composable
internal fun Modifier.likelyPill(likely: Boolean): Modifier {
    val color = MaterialTheme.colorScheme.secondaryContainer
    val shown by animateFloatAsState(
        if (likely) 1f else 0f,
        MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "likely-pill",
    )
    return drawBehind {
        if (shown > 0f) {
            drawRoundRect(
                color.copy(alpha = shown * PILL_ALPHA),
                cornerRadius = CornerRadius(size.height / 2),
            )
        }
    }
}

private const val HOURS = 24

/** Score of a favourite opened three times this hour: the least that is a habit. */
private const val MIN_SCORE = 6

/** The likely favourite scores at least 3/2 of the next one. */
private const val LEAD_NUMERATOR = 3
private const val LEAD_DENOMINATOR = 2

/** How strongly the pill shows: a hint behind the name, not a selection. */
private const val PILL_ALPHA = 0.45f
