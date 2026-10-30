package com.wander.mono.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.settings.IconMode
import com.wander.mono.data.settings.IconSettings
import com.wander.mono.data.settings.IconShape
import com.wander.mono.data.settings.SYSTEM_ICON_PACK

/**
 * Full-colour, themed monochrome, or no app icons at all, and the shape they are cut to; in full
 * colour, the icon pack they come from. Apps a pack does not cover keep their own icon.
 */
@Composable
fun IconSection(settings: SettingsViewModel, iconPacks: IconPackViewModel = viewModel()) {
    val mode = settings.collect(IconSettings.Mode)
    SettingsGroup(R.string.settings_icons) {
        ChoiceSetting(settings, IconSettings.Mode, R.string.settings_icon_mode, R.drawable.sym_apps) { choice ->
            when (choice) {
                IconMode.Colour -> R.string.settings_icon_colour
                IconMode.Themed -> R.string.settings_icon_themed
                IconMode.TextOnly -> R.string.settings_icon_text_only
            }
        }
        if (mode != null && mode != IconMode.TextOnly) {
            ChoiceSetting(
                settings,
                IconSettings.Shape,
                R.string.settings_icon_shape,
                R.drawable.sym_interests,
            ) { shape ->
                when (shape) {
                    IconShape.System -> R.string.settings_icon_shape_system
                    IconShape.Circle -> R.string.settings_icon_shape_circle
                    IconShape.Squircle -> R.string.settings_icon_shape_squircle
                    IconShape.Cookie -> R.string.settings_icon_shape_cookie
                    IconShape.Clover -> R.string.settings_icon_shape_clover
                }
            }
        }
    }
    if (mode == IconMode.Colour) IconPackGroup(settings, iconPacks)
}

/** The icon pack picker: the apps' own icons first, then each installed pack. */
@Composable
private fun IconPackGroup(settings: SettingsViewModel, iconPacks: IconPackViewModel) {
    val selected = settings.collect(IconSettings.Pack) ?: return
    val packs by iconPacks.packs.collectAsStateWithLifecycle()
    SettingsGroup(R.string.settings_icon_pack) {
        PackRow(
            label = stringResource(R.string.settings_icon_pack_system),
            selected = selected == SYSTEM_ICON_PACK,
            onSelect = { settings.set(IconSettings.Pack, SYSTEM_ICON_PACK) },
            leading = { SettingIconBadge(R.drawable.sym_interests) },
        )
        val list = packs.orEmpty()
        if (packs != null && list.isEmpty()) {
            ListItem(colors = settingRowColors()) { Text(stringResource(R.string.settings_icon_pack_none)) }
        }
        list.forEach { pack ->
            PackRow(
                label = pack.label,
                selected = selected == pack.packageName,
                onSelect = { settings.set(IconSettings.Pack, pack.packageName) },
                leading = pack.icon?.let { icon ->
                    { Image(icon, contentDescription = null, modifier = Modifier.size(PACK_ICON_SIZE)) }
                },
            )
        }
    }
}

@Composable
private fun PackRow(label: String, selected: Boolean, onSelect: () -> Unit, leading: (@Composable () -> Unit)?) {
    ListItem(
        colors = settingRowColors(),
        leadingContent = leading,
        trailingContent = { RadioButton(selected = selected, onClick = null) },
        modifier = Modifier.selectable(selected, role = Role.RadioButton, onClick = onSelect),
    ) {
        Text(label)
    }
}

private val PACK_ICON_SIZE = 40.dp
