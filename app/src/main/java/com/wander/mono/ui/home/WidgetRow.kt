package com.wander.mono.ui.home

import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.wander.mono.R
import com.wander.mono.core.widgets.MonoWidgetHostView

/**
 * A hosted widget, drawn by its app in a rounded card the width of the column. The widget handles
 * its own taps; a long press opens its options. The provider is told the exact size it gets.
 */
@Composable
fun WidgetRow(
    row: WidgetHomeRow,
    createView: (Context, Int) -> MonoWidgetHostView,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val radius = widgetCornerRadius()
    val shape = RoundedCornerShape(radius)
    val radiusPx = with(LocalDensity.current) { radius.toPx() }
    val currentLongPress by rememberUpdatedState(onLongPress)
    val optionsLabel = stringResource(R.string.action_widget_options)
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(row.widget.placement.heightDp.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .semantics {
                contentDescription = row.widget.label
                onLongClick(optionsLabel) {
                    currentLongPress()
                    true
                }
            },
    ) {
        val widthDp = maxWidth.value.toInt()
        val heightDp = maxHeight.value.toInt()
        AndroidView(
            factory = { context -> createView(context, row.widget.placement.appWidgetId) },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.onLongPress = { currentLongPress() }
                view.cornerRadius = radiusPx
                view.applySize(widthDp, heightDp)
            },
        )
    }
}

/** The system's widget corner radius (Android 12+), so hosted widgets match their own design; else M3 large. */
@Composable
private fun widgetCornerRadius(): Dp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    dimensionResource(android.R.dimen.system_app_widget_background_radius)
} else {
    16.dp
}
