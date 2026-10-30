package com.wander.mono.ui

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.edit.EditToolbarClearance

/**
 * Mono's error and confirmation messages from [launcher], with their Undo where one is offered and
 * a reject haptic for failures, as a snackbar above the bottom inset and clear of what home keeps
 * at its foot: the search dock, or the floating toolbar while [editing].
 * Over the drawer or settings ([overlayOpen]) nothing is there, so it sits on the inset itself.
 */
@Composable
fun LauncherSnackbar(
    launcher: LauncherViewModel,
    overlayOpen: Boolean,
    editing: Boolean,
    modifier: Modifier = Modifier,
) {
    val snackbar = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val view = LocalView.current
    LaunchedEffect(launcher) {
        launcher.messages.collect { message ->
            if (message.isError) view.rejectHaptic()
            val action = message.action
            val result = snackbar.showSnackbar(
                message = resources.getString(message.text),
                actionLabel = action?.let { resources.getString(it.label) },
                duration = if (action == null) SnackbarDuration.Short else SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) action?.onAction?.invoke()
        }
    }
    val clearance by animateDpAsState(
        snackbarClearance(overlayOpen, editing),
        MaterialTheme.motionScheme.defaultSpatialSpec(),
    )
    SnackbarHost(
        snackbar,
        modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            // The expressive spring overshoots: on its way to 0 it would dip below, which padding refuses.
            .padding(bottom = clearance.coerceAtLeast(0.dp)),
    )
}

/** A failure is felt as well as read (spec 6); the reject pattern only exists from Android 11. */
private fun View.rejectHaptic() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) performHapticFeedback(HapticFeedbackConstants.REJECT)
}

/** How far above the bottom inset the snackbar sits, clear of home's dock or edit toolbar. */
private fun snackbarClearance(overlayOpen: Boolean, editing: Boolean): Dp = when {
    overlayOpen -> 0.dp
    editing -> EditToolbarClearance
    else -> DOCK_CLEARANCE
}

/** Home's search dock: its 56 dp pill and the 8 dp above and below it. */
private val DOCK_CLEARANCE = 56.dp + 16.dp
