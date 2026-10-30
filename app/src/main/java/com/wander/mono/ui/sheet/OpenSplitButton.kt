package com.wander.mono.ui.sheet

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.ui.components.instantPress

/**
 * The sheet's main action, an M3 Expressive split button: "Open" filled and wide, and, when the
 * app has shortcuts, a trailing half that drops them down as a menu. Both halves meet with tight
 * inner corners; a press rounds the inner corner and tightens the outer, and the open menu turns
 * the trailing half into a circle with its chevron flipped. A locked app shows a lock on "Open".
 */
@Composable
internal fun OpenSplitButton(
    locked: Boolean,
    shortcuts: List<AppShortcut>,
    onOpen: () -> Unit,
    onShortcut: (AppShortcut) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    val hasMenu = shortcuts.isNotEmpty()
    Row(
        horizontalArrangement = Arrangement.spacedBy(Gap),
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp).height(Height),
    ) {
        val interaction = remember { MutableInteractionSource() }
        val pressed by interaction.collectIsPressedAsState()
        val outer by animateDpAsState(if (pressed) PressedCorner else Height / 2, spatialSpec())
        val inner by animateDpAsState(
            when {
                !hasMenu -> outer
                pressed -> PressedInnerCorner
                else -> InnerCorner
            },
            spatialSpec(),
        )
        Surface(
            onClick = onOpen,
            shape = RoundedCornerShape(topStart = outer, bottomStart = outer, topEnd = inner, bottomEnd = inner),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            interactionSource = interaction,
            modifier = Modifier.weight(1f).fillMaxHeight().instantPress(interaction),
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Icon(painterResource(if (locked) R.drawable.sym_lock else R.drawable.sym_arrow_outward), null)
                Text(
                    stringResource(R.string.action_open),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        if (hasMenu) {
            Box {
                MenuHalf(expanded = menuOpen, onToggle = { menuOpen = !menuOpen })
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    shape = MaterialTheme.shapes.large,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    shortcuts.forEach { shortcut ->
                        DropdownMenuItem(
                            text = { Text(shortcut.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingIcon = { Icon(painterResource(R.drawable.sym_arrow_outward), null) },
                            onClick = {
                                menuOpen = false
                                onShortcut(shortcut)
                            },
                        )
                    }
                }
            }
        }
    }
}

/** The trailing half: a chevron that flips, in a shape that turns round while the menu is open. */
@Composable
private fun MenuHalf(expanded: Boolean, onToggle: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val outer by animateDpAsState(if (pressed && !expanded) PressedCorner else Height / 2, spatialSpec())
    val inner by animateDpAsState(
        when {
            expanded -> Height / 2
            pressed -> PressedInnerCorner
            else -> InnerCorner
        },
        spatialSpec(),
    )
    val turn by animateFloatAsState(if (expanded) HALF_TURN else 0f, spatialSpec())
    val state = stringResource(if (expanded) R.string.sheet_shortcuts_shown else R.string.sheet_shortcuts_hidden)
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(topStart = inner, bottomStart = inner, topEnd = outer, bottomEnd = outer),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        interactionSource = interaction,
        modifier = Modifier
            .width(MenuWidth)
            .fillMaxHeight()
            .instantPress(interaction)
            .semantics {
                role = Role.DropdownList
                stateDescription = state
            },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painterResource(R.drawable.sym_keyboard_arrow_down),
                contentDescription = stringResource(R.string.sheet_shortcuts),
                modifier = Modifier.size(28.dp).graphicsLayer { rotationZ = turn },
            )
        }
    }
}

@Composable
private fun <T> spatialSpec() = MaterialTheme.motionScheme.fastSpatialSpec<T>()

private val Height = 64.dp
private val MenuWidth = 64.dp

/** M3's split button gap: the halves read as one button. */
private val Gap = 2.dp
private val InnerCorner = 6.dp
private val PressedInnerCorner = 14.dp
private val PressedCorner = 18.dp
private const val HALF_TURN = 180f
