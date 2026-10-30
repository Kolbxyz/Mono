package com.wander.mono.ui.edit

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.data.settings.RowDensity
import com.wander.mono.ui.home.densityLabel
import com.wander.mono.ui.theme.secondaryTextColor

/**
 * Edit mode's floating toolbar, in the thumb zone: add an app or a widget, the row density (named,
 * each press the next one), where a short list sits (a slider), settings, and Done.
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
    edit: EditHomeViewModel = viewModel(),
) {
    val density by edit.density.collectAsStateWithLifecycle()
    val position by edit.listPosition.collectAsStateWithLifecycle()
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(motion.defaultSpatialSpec()) { it } + fadeIn(motion.defaultEffectsSpec()),
        exit = slideOutVertically(motion.fastSpatialSpec()) { it } + fadeOut(motion.fastEffectsSpec()),
    ) {
        Toolbar(onDone, onAddApp, onAddWidget, onOpenSettings, density, edit::cycleDensity) {
            ListPositionButton(position, edit::setListPosition)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Toolbar(
    onDone: () -> Unit,
    onAddApp: () -> Unit,
    onAddWidget: () -> Unit,
    onOpenSettings: () -> Unit,
    density: RowDensity,
    onCycleDensity: () -> Unit,
    position: @Composable () -> Unit,
) {
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
        DensityButton(density, onCycleDensity)
        position()
        IconButton(onClick = onOpenSettings) {
            Icon(painterResource(R.drawable.sym_settings), contentDescription = stringResource(R.string.edit_settings))
        }
    }
}

/**
 * The row density as a small labelled button: "Rows" over the density it is on, so it reads
 * without a guess; each press moves to the next one, shown in place.
 */
@Composable
private fun DensityButton(density: RowDensity, onCycle: () -> Unit) {
    val densityName = stringResource(R.string.edit_density, stringResource(densityLabel(density)))
    TextButton(
        onClick = onCycle,
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = Modifier.semantics { contentDescription = densityName },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clearAndSetSemantics {}) {
            Text(
                stringResource(R.string.edit_density_caption),
                style = MaterialTheme.typography.labelSmall,
                color = secondaryTextColor(),
            )
            Text(stringResource(shortDensityLabel(density)), style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** A density's name short enough for the toolbar. */
@StringRes
private fun shortDensityLabel(density: RowDensity): Int = when (density) {
    RowDensity.Compact -> R.string.density_compact
    RowDensity.Default -> R.string.density_default
    RowDensity.Comfortable -> R.string.edit_density_roomy
}

/** The toolbar's gap above the bottom inset. */
private val TOOLBAR_MARGIN = 16.dp

/**
 * How much of the bottom of home the toolbar covers, measured from the bottom safe-drawing inset
 * (which home already pads): its height, its margin and a gap. Home's list pads its end by this
 * in edit mode, so the last row scrolls clear of the toolbar.
 */
val EditToolbarClearance: Dp = FloatingToolbarDefaults.ContainerSize + TOOLBAR_MARGIN + 8.dp
