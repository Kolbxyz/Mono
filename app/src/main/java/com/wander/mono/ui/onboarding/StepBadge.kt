package com.wander.mono.ui.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import com.wander.mono.R

/**
 * A step's picture above its title: its glyph on a shaped tonal badge, the same family as the
 * settings hub's, so setup and settings look like one app. Decorative: the title says it all.
 */
@Composable
internal fun StepBadge(step: OnboardingStep, modifier: Modifier = Modifier) {
    val badge = badgeOf(step) ?: return
    val scheme = MaterialTheme.colorScheme
    val (container, glyph) = when (step.ordinal % THEME_ROLES) {
        0 -> scheme.primaryContainer to scheme.onPrimaryContainer
        1 -> scheme.secondaryContainer to scheme.onSecondaryContainer
        else -> scheme.tertiaryContainer to scheme.onTertiaryContainer
    }
    Box(
        modifier.size(BadgeSize).background(container, badge.shape.toShape()),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(badge.icon), contentDescription = null, tint = glyph, modifier = Modifier.size(GlyphSize))
    }
}

private class StepArt(val shape: RoundedPolygon, @DrawableRes val icon: Int)

/** Each step's badge; the favourites step has its list instead. */
private fun badgeOf(step: OnboardingStep): StepArt? = when (step) {
    OnboardingStep.Welcome -> StepArt(MaterialShapes.Cookie9Sided, R.drawable.sym_interests)
    OnboardingStep.DefaultHome -> StepArt(MaterialShapes.Clover4Leaf, R.drawable.sym_apps)
    OnboardingStep.Favorites -> null
    OnboardingStep.NotificationAccess -> StepArt(MaterialShapes.Square, R.drawable.sym_notifications)
    OnboardingStep.Calendar -> StepArt(MaterialShapes.Cookie12Sided, R.drawable.sym_event)
    OnboardingStep.Done -> StepArt(MaterialShapes.Sunny, R.drawable.sym_check)
}

private val BadgeSize = 72.dp
private val GlyphSize = 36.dp

/** Primary, secondary and tertiary containers, in turn. */
private const val THEME_ROLES = 3
