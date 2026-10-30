package com.wander.mono.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wander.mono.R
import com.wander.mono.data.settings.HomeLookSettings
import com.wander.mono.data.settings.LABEL_SCALE_STEP
import com.wander.mono.data.settings.LIST_POSITION_STEP
import com.wander.mono.ui.home.densityLabel
import com.wander.mono.ui.home.listPositionLabel

/**
 * How home stays readable over the wallpaper, how roomy and large its rows are, where a short list
 * sits and where the clock sits.
 */
@Composable
fun HomeLookSection(settings: SettingsViewModel) {
    SettingsGroup(R.string.settings_home_look) {
        SwitchSetting(
            settings,
            HomeLookSettings.TextShadow,
            title = R.string.settings_text_shadow,
            summary = R.string.settings_text_shadow_short,
            icon = R.drawable.sym_ev_shadow,
        )
        ChoiceSetting(settings, HomeLookSettings.Density, R.string.settings_row_density, R.drawable.sym_view_agenda) {
            densityLabel(it)
        }
        SliderSetting(
            settings,
            HomeLookSettings.LabelScale,
            title = R.string.settings_label_scale,
            step = LABEL_SCALE_STEP,
            icon = R.drawable.sym_format_size,
        ) { stringResource(R.string.settings_label_scale_value, it) }
        SliderSetting(
            settings,
            HomeLookSettings.ListPosition,
            title = R.string.settings_list_position,
            step = LIST_POSITION_STEP,
            icon = R.drawable.sym_view_agenda,
        ) { listPositionLabel(it) }
        SwitchSetting(
            settings,
            HomeLookSettings.Reachable,
            title = R.string.settings_reachable_layout,
            summary = R.string.settings_reachable_layout_short,
            icon = R.drawable.sym_swipe,
        )
    }
}
