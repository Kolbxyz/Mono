package com.wander.mono.ui.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.widgets.WidgetSize
import com.wander.mono.ui.home.WidgetHomeRow
import com.wander.mono.ui.widgets.WidgetSizeButtons
import com.wander.mono.ui.widgets.label

/**
 * A widget in edit mode, Niagara style: a compact card in its place on home instead of the live
 * widget, with drag handle, name and size, the size buttons and removal right on it.
 */
@Composable
internal fun EditWidgetRow(
    row: WidgetHomeRow,
    lifted: Boolean,
    onResize: (WidgetSize) -> Unit,
    onRemove: () -> Unit,
    handle: Modifier,
    modifier: Modifier = Modifier,
) {
    val lift = animateLift(lifted)
    val size = WidgetSize.nearest(row.widget.placement.heightDp)
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .liftScale(lift)
            .editCard(lift)
            .padding(end = 4.dp, bottom = 12.dp),
    ) {
        Row(Modifier.heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
            DragHandle(handle)
            Icon(
                painterResource(R.drawable.sym_widgets),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp).size(24.dp),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    row.widget.label,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(R.string.edit_widget_kind, stringResource(size.label)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    painterResource(R.drawable.sym_remove_circle),
                    contentDescription = stringResource(R.string.widget_remove),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
        WidgetSizeButtons(size, onResize, Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp))
    }
}
