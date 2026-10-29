package com.wander.mono.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.data.settings.BooleanSetting
import com.wander.mono.data.settings.EnumSetting
import com.wander.mono.data.settings.StoredEnum

/** A titled group of setting rows. */
@Composable
fun SettingsGroup(@StringRes title: Int, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            stringResource(title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
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

/** A choice among an enum's values, as a connected button group under the title. */
@Composable
fun <E> ChoiceSetting(
    settings: SettingsViewModel,
    setting: EnumSetting<E>,
    @StringRes title: Int,
    label: (E) -> Int,
) where E : Enum<E>, E : StoredEnum {
    val selected = settings.collect(setting) ?: return
    val last = setting.values.lastIndex
    ListItem(
        supportingContent = {
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp).selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                setting.values.forEachIndexed { index, value ->
                    ToggleButton(
                        checked = value == selected,
                        onCheckedChange = { settings.set(setting, value) },
                        modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            last -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                    ) {
                        Text(stringResource(label(value)))
                    }
                }
            }
        },
    ) {
        Text(stringResource(title))
    }
}
