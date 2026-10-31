package com.wander.mono.ui.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.data.settings.EnumSetting
import com.wander.mono.data.settings.StoredEnum

/**
 * A choice among an enum's values under the title. Without a [preview] the values are a connected
 * button group while their labels fit on one line at their full width, and separate pills on as
 * many lines as they need otherwise ([ChoiceGroup]): a label is never cut to a couple of letters.
 * With a [preview] each value is a tile showing it (a shape, a colour swatch, a glyph: see
 * [ChoiceShapePreview], [ChoiceSwatchPreview], [ChoiceIconPreview]) over its label, or with
 * [captions] off over nothing, the label then only read out ([ChoiceTiles]).
 */
@Composable
fun <E> ChoiceSetting(
    settings: SettingsViewModel,
    setting: EnumSetting<E>,
    @StringRes title: Int,
    @DrawableRes icon: Int? = null,
    preview: (@Composable (E) -> Unit)? = null,
    captions: Boolean = true,
    label: (E) -> Int,
) where E : Enum<E>, E : StoredEnum {
    ChoiceTextSetting(settings, setting, title, icon, preview = preview, captions = captions) {
        stringResource(label(it))
    }
}

/**
 * [ChoiceSetting] with labels built at run time (a date written in each format, say). [wrap] lays
 * the buttons out as separate pills from the start, for labels that read better apart even when
 * they would fit together.
 */
@Composable
fun <E> ChoiceTextSetting(
    settings: SettingsViewModel,
    setting: EnumSetting<E>,
    @StringRes title: Int,
    @DrawableRes icon: Int? = null,
    wrap: Boolean = false,
    preview: (@Composable (E) -> Unit)? = null,
    captions: Boolean = true,
    label: @Composable (E) -> String,
) where E : Enum<E>, E : StoredEnum {
    val selected = settings.collect(setting) ?: return
    val values = setting.values
    if (preview != null) {
        // Tiles take the row's full width, not the strip beside the badge: more of them to a line.
        Column(Modifier.selectableGroup()) {
            ListItem(colors = settingRowColors(), leadingContent = leadingBadge(icon)) { Text(stringResource(title)) }
            ChoiceTiles(values.size, Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                val value = values[it]
                ChoiceTileItem(
                    selected = value == selected,
                    label = label(value),
                    captions = captions,
                    onClick = { settings.set(setting, value) },
                    preview = { preview(value) },
                )
            }
        }
        return
    }
    ListItem(
        colors = settingRowColors(),
        leadingContent = leadingBadge(icon),
        supportingContent = {
            val group = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp).selectableGroup()
            ChoiceGroup(values.size, wrap, group) { index, shapes, modifier ->
                val value = values[index]
                ChoiceButton(value == selected, label(value), shapes, modifier) { settings.set(setting, value) }
            }
        },
    ) {
        Text(stringResource(title))
    }
}

/**
 * One value as a toggle button: [shapes] connect it to its neighbours in a row, null keeps the
 * standalone pill. Its label takes a second line rather than an ellipsis when it is alone too wide.
 */
@Composable
private fun ChoiceButton(
    checked: Boolean,
    label: String,
    shapes: ToggleButtonShapes?,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val radio = modifier.semantics { role = Role.RadioButton }
    val onChecked: (Boolean) -> Unit = { onClick() }
    val content: @Composable RowScope.() -> Unit = {
        Text(label, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
    if (shapes == null) {
        ToggleButton(checked, onChecked, radio, colors = choiceColors(), content = content)
    } else {
        ToggleButton(checked, onChecked, radio, shapes = shapes, colors = choiceColors(), content = content)
    }
}

/** Unchecked choices sit a step above the group's strip, so they read as buttons on it. */
@Composable
internal fun choiceColors(): ToggleButtonColors =
    ToggleButtonDefaults.toggleButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
