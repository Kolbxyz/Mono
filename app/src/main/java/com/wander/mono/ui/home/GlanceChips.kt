package com.wander.mono.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.system.BatteryState
import com.wander.mono.core.system.LOW_BATTERY_PERCENT
import com.wander.mono.ui.components.ROW_PRESSED_SCALE
import com.wander.mono.ui.components.expressivePress
import com.wander.mono.ui.components.instantPress
import com.wander.mono.ui.theme.secondaryTextColor

/**
 * One glance fact as a pill under the clock: a symbol and a few words on a tonal capsule, so the
 * header reads at a look rather than line by line. [description] is what TalkBack says in place of
 * the short [label]. With [onClick] it is a button that shrinks under the finger; a long press
 * ([onLongClick]) is its own and never home's options. [alert] draws it in the error colours.
 */
@Composable
internal fun GlancePill(
    @DrawableRes icon: Int,
    label: String,
    description: String,
    modifier: Modifier = Modifier,
    alert: Boolean = false,
    onClickLabel: String? = null,
    onClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val container = if (alert) scheme.errorContainer else scheme.surfaceContainerHigh
    val content = if (alert) scheme.onErrorContainer else secondaryTextColor()
    val interaction = remember { MutableInteractionSource() }
    val presses = if (onClick != null) {
        Modifier
            .instantPress(interaction)
            .combinedClickable(
                interactionSource = interaction,
                indication = ripple(),
                role = Role.Button,
                onClickLabel = onClickLabel,
                onLongClickLabel = onLongClickLabel,
                onLongClick = onLongClick,
                onClick = onClick,
            )
    } else {
        Modifier
    }
    Row(
        modifier
            .minimumInteractiveComponentSize()
            .widthIn(max = PILL_MAX_WIDTH)
            .expressivePress(interaction, pressedScale = PILL_PRESSED_SCALE)
            .clip(CircleShape)
            .background(container)
            .then(presses)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .heightIn(min = PILL_HEIGHT)
            .padding(start = 10.dp, end = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PillContent(icon, label, content)
    }
}

@Composable
private fun PillContent(@DrawableRes icon: Int, label: String, content: Color) {
    Icon(painterResource(icon), contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
    Text(
        label,
        style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
        color = content,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * Battery level, shown only when low, charging or saving; it opens nothing, so it is not a button.
 * Low and not charging, it takes the error colours: the one glance fact that asks for something.
 * Under battery saver the leaf stands in for the battery, unless a charger is feeding it.
 */
@Composable
internal fun BatteryChip(battery: BatteryState, modifier: Modifier = Modifier) {
    val (icon, description) = when {
        battery.charging -> R.drawable.sym_battery_charging_full to R.string.glance_battery_charging
        battery.saver -> R.drawable.sym_eco to R.string.glance_battery_saver
        else -> R.drawable.sym_battery_full to R.string.glance_battery_low
    }
    GlancePill(
        icon = icon,
        label = stringResource(R.string.glance_battery_percent, battery.percent),
        description = stringResource(description, battery.percent),
        alert = !battery.charging && battery.percent <= LOW_BATTERY_PERCENT,
        modifier = modifier,
    )
}

/**
 * Quiet mode while it is on (spec 7.9): the moon and, when it ends by itself, only the end time,
 * since the symbol already says "quiet". A tap offers to end it now or keep it one more hour.
 * [onEnd] and [onExtend] are null in the settings preview.
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
    val end = if (quiet.until > 0) {
        remember(quiet.until, timeSkeleton, locale) { format(quiet.until, timeSkeleton, locale) }
    } else {
        null
    }
    Box(modifier) {
        GlancePill(
            icon = R.drawable.sym_quiet,
            label = end ?: stringResource(R.string.glance_quiet),
            description = end?.let { stringResource(R.string.glance_quiet_until, it) }
                ?: stringResource(R.string.glance_quiet),
            onClick = onEnd?.let { { menu = true } },
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

/** Pills are small: they shrink further than a wide row does, so the press still shows. */
private const val PILL_PRESSED_SCALE = ROW_PRESSED_SCALE - 0.04f

/** The capsule's own height; [minimumInteractiveComponentSize] keeps the touch target at 48 dp. */
private val PILL_HEIGHT = 32.dp

/** A long event title ends in an ellipsis rather than pushing the other pills to a new line. */
private val PILL_MAX_WIDTH = 280.dp
