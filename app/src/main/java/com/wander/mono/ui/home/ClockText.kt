package com.wander.mono.ui.home

import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wander.mono.data.settings.ClockAlignment
import com.wander.mono.data.settings.ClockLook
import com.wander.mono.data.settings.ClockStyle
import com.wander.mono.ui.theme.CLOCK_MAX_FONT_SCALE
import com.wander.mono.ui.theme.DayPhase
import com.wander.mono.ui.theme.capFontScale
import com.wander.mono.ui.theme.clockStyle
import com.wander.mono.ui.theme.dayPhaseAt
import com.wander.mono.ui.theme.dayWeight
import com.wander.mono.ui.theme.decorativeMotion
import com.wander.mono.ui.theme.steppedWeight
import com.wander.mono.ui.theme.textAlpha
import java.util.Date
import java.util.Locale

/**
 * The time in the user's [look]: one line, hours over minutes, or one small line. Each character
 * rolls up on its own when it changes, on the theme's motion (instant with reduced motion); glyphs
 * keep their place by field, so a new hour digit does not roll every glyph after it.
 * [compact] (a phone in landscape) shrinks it so the list keeps room. It follows the day ([DayPhase]):
 * a little bolder at midday, lighter and a touch dimmer at night, easing over at each boundary on the
 * effects motion (none while motion is reduced; never dimmed with high contrast).
 */
@Composable
fun ClockText(
    now: Long,
    is24Hour: Boolean,
    look: ClockLook,
    locale: Locale,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val size = clockSize(look, compact)
    val phase = remember(now) { dayPhaseAt(now) }
    val weight = look.weight + dayClockShift(dayWeight(look.weight, phase, look.font.weights) - look.weight)
    val brightness = dayClockBrightness(phase)
    val style = clockStyle(look.font, weight, size.sp).capFontScale(CLOCK_MAX_FONT_SCALE).onWallpaper()
    val horizontal = if (look.alignment == ClockAlignment.Center) Alignment.CenterHorizontally else Alignment.Start
    val dimmed = modifier.graphicsLayer { alpha = brightness.value }
    if (look.style == ClockStyle.Stacked) {
        val hours = remember(now, is24Hour, locale) { pattern(now, if (is24Hour) "HH" else "hh", locale) }
        val minutes = remember(now, locale) { pattern(now, "mm", locale) }
        Column(dimmed, horizontalAlignment = horizontal) {
            RollingGlyphs(remember(hours) { fieldGlyphs(hours, "hour") }, style)
            RollingGlyphs(remember(minutes) { fieldGlyphs(minutes, "minute") }, style)
        }
    } else {
        val skeleton = if (is24Hour) "Hm" else "hm"
        val glyphs = remember(now, skeleton, locale) { clockGlyphs(now, skeleton, locale) }
        Column(dimmed, horizontalAlignment = horizontal) { RollingGlyphs(glyphs, style) }
    }
}

/**
 * The day's shift of the clock's weight easing to [target] when the day crosses a boundary, in steps
 * (see [steppedWeight]) and landing on [target] itself. Only the shift eases: the user's own weight
 * follows the slider at once. Frames only run while home is shown.
 */
@Composable
private fun dayClockShift(target: Int): Int {
    val animated by animateIntAsState(target, decorativeMotion().slowEffectsSpec(), label = "clock-weight")
    return if (animated == target) target else steppedWeight(animated)
}

/** The clock's opacity for [phase], read at draw time; always full with high contrast. */
@Composable
private fun dayClockBrightness(phase: DayPhase): State<Float> =
    animateFloatAsState(textAlpha(phase.brightness), decorativeMotion().slowEffectsSpec(), label = "clock-dim")

/** The clock's size in sp for its style; landscape caps it so the list keeps room. */
internal fun clockSize(look: ClockLook, compact: Boolean): Int {
    val size = if (look.style == ClockStyle.Small) look.size * SMALL_FRACTION else look.size.toFloat()
    return (if (compact) size * COMPACT_FRACTION else size).toInt()
}

/**
 * The glyphs in a row, each rolling in its [ClockGlyph.slot]. The roll is clipped above and below,
 * with [ROLL_SLACK] for the text shadow, and never at the sides, where a glyph's shadow or halo
 * reaches over its neighbours. Glyphs align at the top, since [AnimatedContent] does not carry a
 * baseline; the smaller marker is pushed down by the measured gap so it sits on the digits' baseline.
 */
@Composable
private fun RollingGlyphs(glyphs: List<ClockGlyph>, style: TextStyle) {
    val motion = MaterialTheme.motionScheme
    val density = LocalDensity.current
    val slack = with(density) { ROLL_SLACK.toPx() }
    val markerStyle = style.copy(fontSize = style.fontSize * MARKER_FRACTION)
    val measurer = rememberTextMeasurer()
    val markerDrop = remember(style, measurer, density) {
        val digit = measurer.measure("0", style).firstBaseline
        val marker = measurer.measure("M", markerStyle).firstBaseline
        with(density) { (digit - marker).coerceAtLeast(0f).toDp() }
    }
    Row(
        Modifier.drawWithContent {
            clipRect(-size.width, -slack, size.width * 2, size.height + slack) { this@drawWithContent.drawContent() }
        },
        verticalAlignment = Alignment.Top,
    ) {
        glyphs.forEach { glyph ->
            key(glyph.slot) {
                AnimatedContent(
                    targetState = glyph,
                    transitionSpec = {
                        val enter = slideInVertically(motion.defaultSpatialSpec()) { it } +
                            fadeIn(motion.defaultEffectsSpec())
                        val exit = slideOutVertically(motion.defaultSpatialSpec()) { -it } +
                            fadeOut(motion.fastEffectsSpec())
                        (enter togetherWith exit)
                            .using(SizeTransform(clip = false) { _, _ -> motion.defaultSpatialSpec() })
                    },
                    label = "clockGlyph",
                ) { shown ->
                    Text(
                        shown.char.toString(),
                        modifier = if (shown.marker) Modifier.padding(top = markerDrop) else Modifier,
                        style = if (shown.marker) markerStyle else style,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

private fun pattern(millis: Long, pattern: String, locale: Locale): String =
    SimpleDateFormat(pattern, locale).format(Date(millis))

/** ICU picks the right order and separators for the locale from a skeleton. */
internal fun format(millis: Long, skeleton: String, locale: Locale): String =
    DateFormat.getInstanceForSkeleton(skeleton, locale).format(Date(millis))

/** The AM/PM marker, relative to the clock digits. */
private const val MARKER_FRACTION = 0.4f

private const val SMALL_FRACTION = 0.5f
private const val COMPACT_FRACTION = 0.64f

/** Room above and below the rolling glyphs for the text shadow or halo, which the roll's clip would cut. */
private val ROLL_SLACK = 16.dp
