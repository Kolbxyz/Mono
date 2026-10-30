package com.wander.mono.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Expressive shape scale, rounder than baseline Material, matching Wanda and Phalanx. The three
 * Expressive steps sit between and past them at the same pace, so a component that reaches for
 * `largeIncreased` (a pressed or selected large container) never ends up squarer than `large`.
 */
internal val MonoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(36.dp),
    largeIncreased = RoundedCornerShape(30.dp),
    extraLargeIncreased = RoundedCornerShape(42.dp),
    extraExtraLarge = RoundedCornerShape(52.dp),
)
