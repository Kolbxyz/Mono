package com.wander.mono.ui.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.data.settings.EnumSetting
import com.wander.mono.data.settings.StoredEnum
import com.wander.mono.ui.theme.isLargeFontScale

/**
 * A choice among an enum's values, as a connected button group under the title. Above 1.5× font
 * scale the buttons wrap onto as many lines as their labels need instead of squeezing into one.
 */
@Composable
fun <E> ChoiceSetting(
    settings: SettingsViewModel,
    setting: EnumSetting<E>,
    @StringRes title: Int,
    @DrawableRes icon: Int? = null,
    label: (E) -> Int,
) where E : Enum<E>, E : StoredEnum {
    ChoiceTextSetting(settings, setting, title, icon) { stringResource(label(it)) }
}

/**
 * [ChoiceSetting] with labels built at run time (a date written in each format, say). [wrap] lays
 * the buttons out as separate pills on as many lines as they need, for labels too long to share one.
 */
@Composable
fun <E> ChoiceTextSetting(
    settings: SettingsViewModel,
    setting: EnumSetting<E>,
    @StringRes title: Int,
    @DrawableRes icon: Int? = null,
    wrap: Boolean = false,
    label: @Composable (E) -> String,
) where E : Enum<E>, E : StoredEnum {
    val selected = settings.collect(setting) ?: return
    val last = setting.values.lastIndex
    // Connected shapes only make sense in one row; wrapped buttons keep the standalone shape.
    val choice: @Composable (E, ToggleButtonShapes?, Modifier) -> Unit = { value, shapes, modifier ->
        val checked = value == selected
        val onChecked: (Boolean) -> Unit = { settings.set(setting, value) }
        val radio = modifier.semantics { role = Role.RadioButton }
        val content: @Composable RowScope.() -> Unit = {
            Text(label(value), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (shapes == null) {
            ToggleButton(checked, onChecked, radio, colors = choiceColors(), content = content)
        } else {
            ToggleButton(checked, onChecked, radio, shapes = shapes, colors = choiceColors(), content = content)
        }
    }
    ListItem(
        colors = settingRowColors(),
        leadingContent = leadingBadge(icon),
        supportingContent = {
            val group = Modifier.fillMaxWidth().padding(top = 8.dp).selectableGroup()
            if (wrap || isLargeFontScale()) {
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

/** Unchecked choices sit a step above the group's strip, so they read as buttons on it. */
@Composable
internal fun choiceColors(): ToggleButtonColors =
    ToggleButtonDefaults.toggleButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
