package com.wander.mono.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wander.mono.R
import com.wander.mono.data.settings.LIST_POSITIONS

/** What the list position slider and edit mode's Position control call [percent]: top, bottom or between. */
@Composable
fun listPositionLabel(percent: Int): String = when (percent) {
    LIST_POSITIONS.first -> stringResource(R.string.list_position_top)
    LIST_POSITIONS.last -> stringResource(R.string.list_position_bottom)
    else -> stringResource(R.string.list_position_percent, percent)
}
