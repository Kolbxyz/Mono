package com.wander.mono.ui.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.data.settings.BooleanSetting

/** One on/off part of a [ToggleChipsSetting]: its setting, its short name and its glyph. */
internal class ToggleChip(
    val setting: BooleanSetting,
    @param:StringRes val label: Int,
    @param:DrawableRes val icon: Int,
)

/**
 * Several on/off settings of one thing (the lines under the clock, the kinds of live row) as one
 * row: a toggle button per part, glyph and one word, on as many lines as they need. Seen at a
 * glance, where a switch row each would be a column of near-identical titles.
 */
@Composable
internal fun ToggleChipsSetting(
    settings: SettingsViewModel,
    @StringRes title: Int,
    chips: List<ToggleChip>,
    @DrawableRes icon: Int? = null,
) {
    val values = chips.map { settings.collect(it.setting) }
    if (values.any { it == null }) return
    ListItem(
        colors = settingRowColors(),
        leadingContent = leadingBadge(icon),
        supportingContent = {
            FlowRow(
                Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                chips.forEachIndexed { index, chip ->
                    ToggleButton(
                        checked = values[index] == true,
                        onCheckedChange = { settings.set(chip.setting, it) },
                        colors = choiceColors(),
                    ) {
                        Icon(painterResource(chip.icon), contentDescription = null, modifier = Modifier.size(ChipGlyph))
                        Text(stringResource(chip.label), Modifier.padding(start = 8.dp), maxLines = 1)
                    }
                }
            }
        },
    ) {
        Text(stringResource(title))
    }
}

private val ChipGlyph = 18.dp
