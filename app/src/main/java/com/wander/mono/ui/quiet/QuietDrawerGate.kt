package com.wander.mono.ui.quiet

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.ui.components.readableColumn
import com.wander.mono.ui.drawer.drawerBackdrop

/**
 * In quiet mode the drawer opens on a pause instead of the app list: all apps show only after an
 * explicit tap, the pause fading into the drawer. The choice lives as long as this composable, so
 * each opening of the drawer asks again. Its backdrop follows the drawer as it is [revealed] (0 to 1).
 */
@Composable
fun QuietDrawerGate(
    quiet: Boolean,
    revealed: () -> Float,
    onClose: () -> Unit,
    modes: QuietViewModel = viewModel(),
    drawer: @Composable () -> Unit,
) {
    var showAll by rememberSaveable { mutableStateOf(false) }
    val look by modes.drawerLook.collectAsStateWithLifecycle()
    // Read here, for as long as the drawer is up, not inside the gate: with blur on, the gate leaving
    // after its fade would otherwise switch off the window blur the drawer has just turned on.
    val backdrop = drawerBackdrop(look, revealed)
    Crossfade(
        targetState = !quiet || showAll,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "quietGate",
    ) { open ->
        if (open) drawer() else Gate(backdrop, onShowAll = { showAll = true }, onClose = onClose)
    }
}

/** The pause itself, on the drawer's own background; it scrolls when large text or landscape needs it. */
@Composable
private fun Gate(backdrop: Color, onShowAll: () -> Unit, onClose: () -> Unit) {
    Surface(
        Modifier.fillMaxSize(),
        color = backdrop,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        BoxWithConstraints(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    // At least the window's height, so a pause that fits stays centred.
                    .heightIn(min = maxHeight)
                    .readableColumn()
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.Start,
            ) {
                Icon(
                    painterResource(R.drawable.sym_quiet),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                )
                Text(stringResource(R.string.quiet_gate_title), style = MaterialTheme.typography.headlineMedium)
                Text(
                    stringResource(R.string.quiet_gate_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = onShowAll) { Text(stringResource(R.string.quiet_gate_show_all)) }
                TextButton(onClick = onClose) { Text(stringResource(R.string.quiet_gate_back)) }
            }
        }
    }
}
