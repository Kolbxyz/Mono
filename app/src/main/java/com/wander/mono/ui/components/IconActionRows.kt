package com.wander.mono.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * A context menu's actions as rows of icon buttons, a short word under each: one connected M3
 * Expressive button group per [MenuAction.group], in ascending order, every button of a row as
 * wide as the next. A press widens the button while its neighbours give way and tightens its
 * corners; an on/off action is filled and fully round while on. [ActionEmphasis.Danger] is tinted
 * in the error colour. TalkBack hears each button's full label, not the word under it.
 */
@Composable
fun IconActionRows(actions: List<MenuAction>, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(RowGap),
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        actions.groupBy { it.group }.entries.sortedBy { it.key }.forEach { (_, row) ->
            Row(horizontalArrangement = Arrangement.spacedBy(ButtonGap), modifier = Modifier.fillMaxWidth()) {
                row.forEachIndexed { index, action ->
                    IconAction(action, first = index == 0, last = index == row.lastIndex)
                }
            }
        }
    }
}

@Composable
private fun RowScope.IconAction(action: MenuAction, first: Boolean, last: Boolean) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val motion = MaterialTheme.motionScheme
    val weight by animateFloatAsState(if (pressed) PRESSED_GROWTH else 1f, motion.fastSpatialSpec())
    val round = action.selected == true
    val outer by animateDpAsState(if (pressed) PressedCorner else ButtonHeight / 2, motion.fastSpatialSpec())
    val inner by animateDpAsState(
        when {
            pressed -> PressedInnerCorner
            round -> ButtonHeight / 2
            else -> InnerCorner
        },
        motion.fastSpatialSpec(),
    )
    val start = if (first) outer else inner
    val end = if (last) outer else inner
    val (targetContainer, targetContent) = colorsFor(action)
    val container by animateColorAsState(targetContainer, motion.defaultEffectsSpec())
    val content by animateColorAsState(targetContent, motion.defaultEffectsSpec())
    val label = action.labelArg?.let { stringResource(action.label, it) } ?: stringResource(action.label)
    Column(Modifier.weight(weight), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = action.onClick,
            shape = RoundedCornerShape(topStart = start, topEnd = end, bottomEnd = end, bottomStart = start),
            color = container,
            contentColor = content,
            interactionSource = interaction,
            modifier = Modifier
                .fillMaxWidth()
                .height(ButtonHeight)
                .instantPress(interaction)
                .then(action.selected?.let { on -> Modifier.toggleSemantics(on) } ?: Modifier),
        ) {
            // Surface stretches its content to the button: the box centres the icon in it.
            Box(contentAlignment = Alignment.Center) {
                Icon(painterResource(action.icon), contentDescription = label, modifier = Modifier.size(IconSize))
            }
        }
        Text(
            action.shortLabel?.let { stringResource(it) } ?: label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            // The button already says it, in full.
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).clearAndSetSemantics {},
        )
    }
}

/** One neutral, filled primary for a switch that is on, error for removal. */
@Composable
private fun colorsFor(action: MenuAction): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when {
        action.selected == true -> scheme.primary to scheme.onPrimary
        action.emphasis == ActionEmphasis.Danger -> scheme.errorContainer to scheme.onErrorContainer
        else -> scheme.surfaceContainerHighest to scheme.onSurface
    }
}

private val ButtonHeight = 56.dp
private val IconSize = 24.dp
private val RowGap = 12.dp

/** M3's connected button group gap: a row reads as one group. */
private val ButtonGap = 2.dp
private val InnerCorner = 8.dp
private val PressedInnerCorner = 4.dp
private val PressedCorner = 16.dp
private const val PRESSED_GROWTH = 1.25f
