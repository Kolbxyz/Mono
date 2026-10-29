package com.wander.mono.ui.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.ListItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.wander.mono.data.settings.IntSetting
import kotlin.math.roundToInt

/**
 * A number setting on a slider with a stop every [step] across [range] (the setting's own range
 * unless a narrower one applies). Each new stop is saved as the thumb reaches it, so a preview
 * follows the drag; the thumb itself follows the finger in between.
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
    ListItem(
        colors = settingRowColors(),
        leadingContent = leadingBadge(icon, enabled),
        trailingContent = { Text(valueLabel(shown.roundToInt())) },
        supportingContent = {
            Slider(
                value = shown,
                onValueChange = { value ->
                    dragging = value
                    val stop = value.roundToInt()
                    if (stop != stored) settings.set(setting, stop)
                },
                onValueChangeFinished = { dragging = null },
                valueRange = range.first.toFloat()..range.last.toFloat(),
                steps = stops,
                enabled = enabled,
            )
        },
    ) {
        Text(stringResource(title))
    }
}
