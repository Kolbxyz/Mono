package com.wander.mono.ui.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.R

/**
 * Edit mode's floating toolbar, in the thumb zone: add an app or a widget, settings, and Done.
 * Slides up while [visible]. It floats over home's list, which keeps [EditToolbarClearance] free.
 */
@Composable
fun EditToolbar(
    visible: Boolean,
    onDone: () -> Unit,
    onAddApp: () -> Unit,
    onAddWidget: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(motion.defaultSpatialSpec()) { it } + fadeIn(motion.defaultEffectsSpec()),
        exit = slideOutVertically(motion.fastSpatialSpec()) { it } + fadeOut(motion.fastEffectsSpec()),
    ) {
        Toolbar(onDone, onAddApp, onAddWidget, onOpenSettings)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Toolbar(onDone: () -> Unit, onAddApp: () -> Unit, onAddWidget: () -> Unit, onOpenSettings: () -> Unit) {
    val editingLabel = stringResource(R.string.state_editing)
    HorizontalFloatingToolbar(
        expanded = true,
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
            .padding(bottom = TOOLBAR_MARGIN)
            .semantics { paneTitle = editingLabel },
        trailingContent = {
            FilledTonalButton(onClick = onDone, modifier = Modifier.padding(start = 8.dp)) {
                Icon(painterResource(R.drawable.sym_check), contentDescription = null)
                Text(stringResource(R.string.edit_done), Modifier.padding(start = 8.dp))
            }
        },
    ) {
        IconButton(onClick = onAddApp) {
            Icon(painterResource(R.drawable.sym_add), contentDescription = stringResource(R.string.edit_add_app))
        }
        IconButton(onClick = onAddWidget) {
            Icon(painterResource(R.drawable.sym_widgets), contentDescription = stringResource(R.string.edit_add_widget))
        }
        IconButton(onClick = onOpenSettings) {
            Icon(painterResource(R.drawable.sym_settings), contentDescription = stringResource(R.string.edit_settings))
        }
    }
}

/** The toolbar's gap above the bottom inset. */
private val TOOLBAR_MARGIN = 16.dp

/**
 * How much of the bottom of home the toolbar covers, measured from the bottom safe-drawing inset
 * (which home already pads): its height, its margin and a gap. Home's list pads its end by this
 * in edit mode, so the last row scrolls clear of the toolbar.
 */
val EditToolbarClearance: Dp = FloatingToolbarDefaults.ContainerSize + TOOLBAR_MARGIN + 8.dp
