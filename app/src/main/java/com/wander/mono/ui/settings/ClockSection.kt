package com.wander.mono.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wander.mono.R
import com.wander.mono.data.settings.CLOCK_SIZE_STEP
import com.wander.mono.data.settings.CLOCK_WEIGHT_STEP
import com.wander.mono.data.settings.ClockAlignment
import com.wander.mono.data.settings.ClockFont
import com.wander.mono.data.settings.ClockSettings
import com.wander.mono.data.settings.ClockStyle
import com.wander.mono.ui.home.format
import com.wander.mono.ui.theme.clockStyle

/** The clock's face: font, weight, size, layout, alignment and how the date reads. */
@Composable
fun ClockSection(settings: SettingsViewModel) {
    val font = settings.collect(ClockSettings.Font) ?: return
    SettingsGroup(R.string.settings_clock) {
        ClockFontRow(settings, font)
        if (font.isVariable) {
            SliderSetting(
                settings,
                ClockSettings.Weight,
                title = R.string.settings_clock_weight,
                step = CLOCK_WEIGHT_STEP,
                icon = R.drawable.sym_format_bold,
                range = weightStops(font),
            ) { it.toString() }
        }
        SliderSetting(
            settings,
            ClockSettings.Size,
            title = R.string.settings_clock_size,
            step = CLOCK_SIZE_STEP,
            icon = R.drawable.sym_format_size,
        ) { it.toString() }
        ChoiceSetting(
            settings,
            ClockSettings.Style,
            R.string.settings_clock_style,
            R.drawable.sym_view_agenda,
        ) { style ->
            when (style) {
                ClockStyle.Default -> R.string.settings_clock_style_default
                ClockStyle.Stacked -> R.string.settings_clock_style_stacked
                ClockStyle.Small -> R.string.settings_clock_style_small
            }
        }
        ChoiceSetting(
            settings,
            ClockSettings.Alignment,
            R.string.settings_clock_alignment,
            R.drawable.sym_format_align_center,
        ) { alignment ->
            when (alignment) {
                ClockAlignment.Start -> R.string.settings_clock_align_start
                ClockAlignment.Center -> R.string.settings_clock_align_center
            }
        }
        val locale = LocalConfiguration.current.locales[0]
        val today = remember { System.currentTimeMillis() }
        ChoiceTextSetting(
            settings,
            ClockSettings.Date,
            R.string.settings_clock_date,
            R.drawable.sym_today,
            wrap = true,
        ) {
            remember(it, locale) { format(today, it.skeleton, locale) }
        }
    }
}

/** The weight slider's stops: the font's own `wght` range, on whole hundreds. */
private fun weightStops(font: ClockFont): IntRange {
    val low = (font.weights.first + CLOCK_WEIGHT_STEP - 1) / CLOCK_WEIGHT_STEP * CLOCK_WEIGHT_STEP
    val high = font.weights.last / CLOCK_WEIGHT_STEP * CLOCK_WEIGHT_STEP
    return low..high
}

/** Each face as a pill that shows its own name in that face, so the choice is seen, not read. */
@Composable
private fun ClockFontRow(settings: SettingsViewModel, selected: ClockFont) {
    ListItem(
        colors = settingRowColors(),
        leadingContent = leadingBadge(R.drawable.sym_font_download),
        supportingContent = {
            FlowRow(
                Modifier.fillMaxWidth().padding(top = 8.dp).selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ClockFont.entries.forEach { font ->
                    ToggleButton(
                        checked = font == selected,
                        onCheckedChange = { settings.set(ClockSettings.Font, font) },
                        modifier = Modifier.semantics { role = Role.RadioButton },
                        colors = choiceColors(),
                    ) {
                        val sample = clockStyle(font, FONT_SAMPLE_WEIGHT, FONT_SAMPLE_SIZE.sp)
                        Text(stringResource(fontName(font)), style = sample)
                    }
                }
            }
        },
    ) {
        Text(stringResource(R.string.settings_clock_font))
    }
}

private fun fontName(font: ClockFont): Int = when (font) {
    ClockFont.Flex -> R.string.settings_clock_font_flex
    ClockFont.Rounded -> R.string.settings_clock_font_rounded
    ClockFont.Serif -> R.string.settings_clock_font_serif
    ClockFont.Mono -> R.string.settings_clock_font_mono
    ClockFont.Condensed -> R.string.settings_clock_font_condensed
}

private const val FONT_SAMPLE_WEIGHT = 600
private const val FONT_SAMPLE_SIZE = 16
