package com.wander.mono.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.theme.LocalReducedMotion

/**
 * The wave's height, 1 while [waving] and 0 otherwise, eased with the theme's fast effects spec.
 * The wave is the playing state, not decoration, so it flattens on pause; with reduced motion it
 * never rises.
 */
@Composable
internal fun rememberWaveAmplitude(waving: Boolean): Animatable<Float, AnimationVector1D> {
    val target = if (waving && !LocalReducedMotion.current) 1f else 0f
    val amplitude = remember { Animatable(target) }
    val spec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    LaunchedEffect(target) { amplitude.animateTo(target, spec) }
    return amplitude
}

/** The wave's travel: none with reduced motion. */
@Composable
internal fun waveSpeed(): Dp =
    if (LocalReducedMotion.current) 0.dp else WavyProgressIndicatorDefaults.LinearDeterminateWavelength

/**
 * A display-only wavy progress line, for places too small for a seek bar. [fraction] is read at
 * draw time, so a ticking position redraws without recomposing.
 */
@Composable
fun WavyProgressLine(fraction: () -> Float, isPlaying: Boolean, color: Color, modifier: Modifier = Modifier) {
    val amplitude = rememberWaveAmplitude(isPlaying)
    LinearWavyProgressIndicator(
        progress = fraction,
        modifier = modifier.fillMaxWidth(),
        color = color,
        trackColor = color.copy(alpha = UNPLAYED_ALPHA),
        amplitude = { amplitude.value },
        waveSpeed = waveSpeed(),
    )
}

/**
 * The seek bar's thumb: a dot at rest, a short upright bar while scrubbing. The same pill shape in
 * both, so only [width] and [height] animate and it traces a morph rather than growing in place.
 */
@Composable
internal fun SeekThumb(width: Dp, height: Dp, color: Color) {
    Box(Modifier.width(width).height(height).background(color, CircleShape))
}

/**
 * The seek bar's track, broken either side of the thumb: the played segment waves, the unplayed one
 * is a thin flat pill. Each keeps half of [thumbWidth] plus a gap clear of the split, so the break
 * follows the thumb as it morphs. Nothing here fills the height: the slider sizes its track slot
 * from whatever the parent has left.
 */
@Composable
internal fun SeekTrack(
    fraction: Float,
    amplitude: Animatable<Float, AnimationVector1D>,
    thumbWidth: Dp,
    color: Color,
) {
    val clearance = thumbWidth / 2 + THUMB_GAP
    val speed = waveSpeed()
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.weight(fraction.coerceAtLeast(MIN_WEIGHT)).padding(end = clearance)) {
            LinearWavyProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxWidth(),
                color = color,
                trackColor = Color.Transparent,
                amplitude = { amplitude.value },
                waveSpeed = speed,
            )
        }
        Box(
            Modifier
                .weight((1f - fraction).coerceAtLeast(MIN_WEIGHT))
                .padding(start = clearance)
                .height(UNPLAYED_HEIGHT)
                .background(color.copy(alpha = UNPLAYED_ALPHA), CircleShape),
        )
    }
}

/** The empty space between the thumb and each segment of the track. */
private val THUMB_GAP = 4.dp

/** The unplayed segment's thickness; the wavy indicator sets the played side's. */
private val UNPLAYED_HEIGHT = 4.dp

/** The unplayed part, in the content colour so it reads on any tinted container. */
private const val UNPLAYED_ALPHA = 0.24f

/** `Modifier.weight` rejects exactly 0: the floor keeps a segment from vanishing at either end. */
private const val MIN_WEIGHT = 0.001f
