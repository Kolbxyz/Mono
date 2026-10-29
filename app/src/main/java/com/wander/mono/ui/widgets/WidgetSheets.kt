package com.wander.mono.ui.widgets

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** The widget picker and a widget row's options sheet, whichever is open. */
@Composable
fun WidgetSheets(widgets: WidgetsViewModel) {
    val pickerOpen by widgets.pickerOpen.collectAsStateWithLifecycle()
    val pickerApps by widgets.pickerApps.collectAsStateWithLifecycle()
    val options by widgets.options.collectAsStateWithLifecycle()

    if (pickerOpen) {
        WidgetPickerSheet(
            apps = pickerApps,
            // WidgetPreview only calls it on Android 12+, where a provider can have a preview layout.
            createPreview = @RequiresApi(Build.VERSION_CODES.S) { context, info ->
                widgets.createPreview(context, info)
            },
            onPick = widgets::pick,
            onDismiss = widgets::closePicker,
        )
    }
    options?.let { state ->
        val id = state.widget.placement.appWidgetId
        WidgetOptionsSheet(
            state = state,
            onDismiss = widgets::dismissOptions,
            onResize = { widgets.resize(id, it) },
            onReconfigure = { widgets.reconfigure(id) },
            onMove = { widgets.move(id, it) },
            onRemove = { widgets.remove(id) },
        )
    }
}
