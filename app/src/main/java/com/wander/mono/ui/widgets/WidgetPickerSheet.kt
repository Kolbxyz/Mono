package com.wander.mono.ui.widgets

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.widgets.WidgetApp
import com.wander.mono.core.widgets.WidgetChoice
import com.wander.mono.ui.components.MonoLoadingIndicator
import com.wander.mono.ui.components.MonoSheet
import com.wander.mono.ui.components.PressMorph
import com.wander.mono.ui.components.expressivePress
import com.wander.mono.ui.components.instantPress
import com.wander.mono.ui.home.HomeCardCorner
import com.wander.mono.ui.sheet.SheetHeader

/**
 * Every widget installed apps offer, grouped under their app (work-profile apps marked), each with
 * its real preview. Picking one starts the add flow; the widget lands under its app when that app
 * is a favourite.
 */
@Composable
fun WidgetPickerSheet(
    apps: List<WidgetApp>?,
    createPreview: (Context, AppWidgetProviderInfo) -> View,
    onPick: (AppWidgetProviderInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    MonoSheet(onDismissRequest = onDismiss) {
        SheetHeader(stringResource(R.string.widget_picker_title))
        when {
            apps == null -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                MonoLoadingIndicator()
            }
            apps.isEmpty() -> Text(
                stringResource(R.string.widget_picker_empty),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(24.dp),
            )
            else -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                apps.forEach { app ->
                    item(key = "app:${app.packageName}:${app.isMainProfile}", contentType = "header") {
                        AppHeader(app)
                    }
                    items(
                        app.widgets,
                        key = { "${it.info.provider.flattenToString()}:${it.info.profile.hashCode()}" },
                        contentType = { "widget" },
                    ) { choice ->
                        WidgetEntry(choice, app.isMainProfile, createPreview) { onPick(choice.info) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppHeader(app: WidgetApp) {
    val work = stringResource(R.string.state_work_profile)
    Text(
        if (app.isMainProfile) app.label else stringResource(R.string.widget_picker_work_app, app.label, work),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun WidgetEntry(
    choice: WidgetChoice,
    isMainProfile: Boolean,
    createPreview: (Context, AppWidgetProviderInfo) -> View,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .expressivePress(interaction, PressMorph.squarer(HomeCardCorner))
            .instantPress(interaction)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                onClickLabel = stringResource(R.string.action_add_widget),
                onClick = onClick,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WidgetPreview(choice.info, isMainProfile, createPreview)
        Text(
            choice.label,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
