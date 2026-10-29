package com.wander.mono.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.wander.mono.R
import com.wander.mono.core.system.BatteryState
import com.wander.mono.ui.theme.secondaryTextColor

/** Battery level, shown only when low or charging; it opens nothing, so it is not a button. */
@Composable
internal fun BatteryChip(battery: BatteryState, modifier: Modifier = Modifier) {
    val icon = if (battery.charging) R.drawable.sym_battery_charging_full else R.drawable.sym_battery_full
    val description = stringResource(
        if (battery.charging) R.string.glance_battery_charging else R.string.glance_battery_low,
        battery.percent,
    )
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(stringResource(R.string.glance_battery_percent, battery.percent)) },
        leadingIcon = { Icon(painterResource(icon), contentDescription = null) },
        modifier = modifier.semantics { contentDescription = description },
        colors = glanceChipColors(),
        border = null,
    )
}

/**
 * "Quiet" while quiet mode is on, with its end time when it has one (spec 7.9); a tap offers to end
 * it now or keep it one more hour. [onEnd] and [onExtend] are null in the settings preview.
 */
@Composable
internal fun QuietChip(
    quiet: QuietGlance,
    timeSkeleton: String,
    onEnd: (() -> Unit)?,
    onExtend: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var menu by rememberSaveable { mutableStateOf(false) }
    val locale = LocalConfiguration.current.locales[0]
    val label = if (quiet.until > 0) {
        val end = remember(quiet.until, timeSkeleton, locale) { format(quiet.until, timeSkeleton, locale) }
        stringResource(R.string.glance_quiet_until, end)
    } else {
        stringResource(R.string.glance_quiet)
    }
    Box(modifier) {
        AssistChip(
            onClick = { menu = true },
            enabled = onEnd != null,
            label = { Text(label) },
            leadingIcon = { Icon(painterResource(R.drawable.sym_quiet), contentDescription = null) },
            colors = glanceChipColors(),
            border = null,
        )
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.glance_quiet_end)) },
                onClick = {
                    menu = false
                    onEnd?.invoke()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.glance_quiet_extend)) },
                onClick = {
                    menu = false
                    onExtend?.invoke()
                },
            )
        }
    }
}

/**
 * Tonal chips on the home scrim. A disabled chip (the battery, or anything in the preview) keeps
 * full-strength colours: it is information, not a dimmed control.
 */
@Composable
private fun glanceChipColors() = with(MaterialTheme.colorScheme) {
    val content = secondaryTextColor()
    AssistChipDefaults.assistChipColors(
        containerColor = surfaceContainerHigh,
        labelColor = content,
        leadingIconContentColor = content,
        disabledContainerColor = surfaceContainerHigh,
        disabledLabelColor = content,
        disabledLeadingIconContentColor = content,
    )
}
