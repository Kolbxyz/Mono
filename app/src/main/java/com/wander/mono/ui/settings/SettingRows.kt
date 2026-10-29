package com.wander.mono.ui.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.data.settings.BooleanSetting
import com.wander.mono.ui.components.CheckSwitch
import com.wander.mono.ui.components.SegmentedColumn

/** A titled group of setting rows, drawn as one segmented card. */
@Composable
fun SettingsGroup(@StringRes title: Int, content: @Composable () -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Text(
            stringResource(title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
                .semantics { heading() },
        )
        SegmentedColumn(content = content)
    }
}

/** Setting rows are transparent: the group's segmented strips are their container. */
@Composable
fun settingRowColors(): ListItemColors = ListItemDefaults.colors(containerColor = Color.Transparent)

/** An on/off setting: the whole row toggles it. [summary] is one short line, only where the title needs it. */
@Composable
fun SwitchSetting(
    settings: SettingsViewModel,
    setting: BooleanSetting,
    @StringRes title: Int,
    @StringRes summary: Int? = null,
    @DrawableRes icon: Int? = null,
) {
    val checked = settings.collect(setting) ?: return
    SwitchRow(checked, { settings.set(setting, it) }, title, summary, icon)
}

/**
 * The row every on/off setting draws: leading badge, title, an optional one-line [summary], the
 * switch. [SwitchRow] and [SwitchSetting] are this with their own source of truth.
 */
@Composable
internal fun SwitchListItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    @DrawableRes icon: Int?,
    summary: String?,
    enabled: Boolean = true,
) {
    ListItem(
        colors = settingRowColors(),
        leadingContent = leadingBadge(icon, enabled),
        supportingContent = summary?.let { { Text(it) } },
        trailingContent = { CheckSwitch(checked) },
        modifier = Modifier.toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange),
    ) {
        Text(title)
    }
}
