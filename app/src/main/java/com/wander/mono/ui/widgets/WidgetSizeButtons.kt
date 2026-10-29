package com.wander.mono.ui.widgets

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wander.mono.R
import com.wander.mono.core.widgets.WidgetSize

/**
 * A widget's three heights as a connected button group, the current one checked. Shared by the
 * widget's options sheet and its card in edit mode, so sizes look and work the same in both.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WidgetSizeButtons(size: WidgetSize, onResize: (WidgetSize) -> Unit, modifier: Modifier = Modifier) {
    val sizes = WidgetSize.entries
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
        sizes.forEachIndexed { index, entry ->
            ToggleButton(
                checked = entry == size,
                onCheckedChange = { onResize(entry) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    sizes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(entry.label), maxLines = 1)
            }
        }
    }
}

/** What a size is called on its button and in the widget's edit card. */
@get:StringRes
val WidgetSize.label: Int
    get() = when (this) {
        WidgetSize.Small -> R.string.widget_size_small
        WidgetSize.Medium -> R.string.widget_size_medium
        WidgetSize.Large -> R.string.widget_size_large
    }
