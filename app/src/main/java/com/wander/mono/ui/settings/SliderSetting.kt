package com.wander.mono.ui.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wander.mono.data.settings.IntSetting
import com.wander.mono.ui.components.rememberMonoHaptics
import kotlin.math.roundToInt

/**
 * A number setting on a slider with a stop every [step] across [range] (the setting's own range
 * unless a narrower one applies). Each new stop is saved as the thumb reaches it, so a preview
 * follows the drag, with a haptic segment tick; the thumb itself follows the finger in between.
 */
@Composable
fun SliderSetting(
    settings: SettingsViewModel,
    setting: IntSetting,
    @StringRes title: Int,
    step: Int,
    @DrawableRes icon: Int? = null,
    range: IntRange = setting.range,
    enabled: Boolean = true,
    valueLabel: @Composable (Int) -> String,
) {
    val stored = settings.collect(setting) ?: return
    var dragging by remember { mutableStateOf<Float?>(null) }
    val shown = dragging ?: stored.coerceIn(range).toFloat()
    val stops = ((range.last - range.first) / step - 1).coerceAtLeast(0)
    val labels = sliderStops(range, step).map { valueLabel(it) }
    val haptics = rememberMonoHaptics()
    ListItem(
        colors = settingRowColors(),
        leadingContent = leadingBadge(icon, enabled),
        supportingContent = {
            Slider(
                value = shown,
                onValueChange = { value ->
                    // The live drag, not this composition's: one tick per stop crossed.
                    val previous = dragging?.roundToInt() ?: stored
                    dragging = value
                    val stop = value.roundToInt()
                    if (stop != previous) haptics.segment()
                    if (stop != stored) settings.set(setting, stop)
                },
                onValueChangeFinished = {
                    dragging = null
                    haptics.gestureEnd()
                },
                valueRange = range.first.toFloat()..range.last.toFloat(),
                steps = stops,
                enabled = enabled,
            )
        },
    ) {
        // The value sits on the title's line, not in a trailing slot: the slider below keeps the
        // row's full width and a long title wraps beside the value instead of being cut.
        Row {
            Text(stringResource(title), Modifier.weight(1f).alignByBaseline().padding(end = 12.dp))
            ValueLabel(valueLabel(shown.roundToInt()), labels, enabled, Modifier.alignByBaseline())
        }
    }
}

/**
 * [text] in tabular digits and the accent colour, as wide as the widest of [all]: the label
 * changing during a drag must not reflow the title beside it.
 */
@Composable
private fun ValueLabel(text: String, all: List<String>, enabled: Boolean, modifier: Modifier) {
    val style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = TABULAR_DIGITS)
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val width = remember(all, style, measurer, density) {
        with(density) { all.maxOf { measurer.measure(it, style).size.width }.toDp() }
    }
    Text(
        text,
        style = style,
        color = if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_ALPHA)
        },
        textAlign = TextAlign.End,
        maxLines = 1,
        modifier = modifier.widthIn(min = width),
    )
}

/** Every value the slider can stop at: [range]'s first, each [step] on, and its last. */
internal fun sliderStops(range: IntRange, step: Int): List<Int> =
    ((range step step) + range.last).distinct()

/** The font feature that gives every digit the same advance. */
private const val TABULAR_DIGITS = "tnum"
