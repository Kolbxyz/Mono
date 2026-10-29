package com.wander.mono.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.data.settings.BooleanSetting
import com.wander.mono.data.settings.EnumSetting
import com.wander.mono.data.settings.StoredEnum
import com.wander.mono.ui.theme.isLargeFontScale

/** A titled group of setting rows. */
@Composable
fun SettingsGroup(@StringRes title: Int, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            stringResource(title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
                .semantics { heading() },
        )
        content()
    }
}

/** An on/off setting: the whole row toggles it. */
@Composable
fun SwitchSetting(
    settings: SettingsViewModel,
    setting: BooleanSetting,
    @StringRes title: Int,
    @StringRes summary: Int? = null,
) {
    val checked = settings.collect(setting) ?: return
    ListItem(
        supportingContent = summary?.let { { Text(stringResource(it)) } },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        modifier = Modifier.toggleable(checked, role = Role.Switch) { settings.set(setting, it) },
    ) {
        Text(stringResource(title))
    }
}

/**
 * A choice among an enum's values, as a connected button group under the title. Above 1.5× font
 * scale the buttons wrap onto as many lines as their labels need instead of squeezing into one.
 */
@Composable
fun <E> ChoiceSetting(
    settings: SettingsViewModel,
    setting: EnumSetting<E>,
    @StringRes title: Int,
    label: (E) -> Int,
) where E : Enum<E>, E : StoredEnum {
    val selected = settings.collect(setting) ?: return
    val last = setting.values.lastIndex
    // Connected shapes only make sense in one row; wrapped buttons keep the standalone shape.
    val choice: @Composable (E, ToggleButtonShapes?, Modifier) -> Unit = { value, shapes, modifier ->
        val checked = value == selected
        val onChecked: (Boolean) -> Unit = { settings.set(setting, value) }
        val radio = modifier.semantics { role = Role.RadioButton }
        val content: @Composable RowScope.() -> Unit = { Text(stringResource(label(value))) }
        if (shapes == null) {
            ToggleButton(checked, onChecked, radio, content = content)
        } else {
            ToggleButton(checked, onChecked, radio, shapes = shapes, content = content)
        }
    }
    ListItem(
        supportingContent = {
            val group = Modifier.fillMaxWidth().padding(top = 8.dp).selectableGroup()
            if (isLargeFontScale()) {
                FlowRow(
                    group,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    setting.values.forEach { choice(it, null, Modifier) }
                }
            } else {
                Row(group, horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                    setting.values.forEachIndexed { index, value ->
                        val shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            last -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        }
                        choice(value, shapes, Modifier.weight(1f))
                    }
                }
            }
        },
    ) {
        Text(stringResource(title))
    }
}
