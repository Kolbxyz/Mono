package com.wander.mono.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.wander.mono.ui.components.CheckSwitch

/**
 * An on/off row whose change goes through a feature's own logic (a permission, a side effect)
 * rather than straight to the store as [SwitchSetting] does.
 */
@Composable
fun SwitchRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    @StringRes title: Int,
    @StringRes summary: Int? = null,
) {
    ListItem(
        colors = settingRowColors(),
        supportingContent = summary?.let { { Text(stringResource(it)) } },
        trailingContent = { CheckSwitch(checked) },
        modifier = Modifier.toggleable(checked, role = Role.Switch, onValueChange = onCheckedChange),
    ) {
        Text(stringResource(title))
    }
}

/** A row that does something when tapped, such as opening Android's page to grant an access. */
@Composable
fun ActionRow(@StringRes title: Int, @StringRes summary: Int? = null, onClick: () -> Unit) {
    ListItem(
        colors = settingRowColors(),
        supportingContent = summary?.let { { Text(stringResource(it)) } },
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(stringResource(title))
    }
}
