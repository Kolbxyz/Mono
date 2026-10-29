package com.wander.mono.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The body of every context menu: buttons in rows, M3 Expressive button-group style. Pressing a
 * button widens it while its row neighbours give way, and its corners tighten.
 *
 * Callers pass a flat list tagged with [ActionEmphasis]; the arrangement is decided here so every
 * menu has the same shape: hero row, icon row, labelled rows in alternating wide/narrow pairs,
 * destructive actions at the foot.
 */
@Composable
fun ActionButtonGroup(actions: List<MenuAction>, modifier: Modifier = Modifier) {
    val hero = actions.filter { it.emphasis == ActionEmphasis.Primary || it.emphasis == ActionEmphasis.Secondary }
    val tonal = actions.filter { it.emphasis == ActionEmphasis.Tonal }
    val icons = actions.filter { it.emphasis == ActionEmphasis.Icon }
    val danger = actions.filter { it.emphasis == ActionEmphasis.Danger }
    Column(
        verticalArrangement = Arrangement.spacedBy(Gap),
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        hero.chunked(2).forEach { ActionRow(it, HeroHeight, wideFirst = null) }
        icons.chunked(4).forEach { ActionRow(it, RowHeight, wideFirst = null) }
        tonal.chunked(2).forEachIndexed { i, row -> ActionRow(row, RowHeight, wideFirst = i % 2 == 0) }
        danger.forEach { ActionRow(listOf(it), RowHeight, wideFirst = null) }
    }
}

/** One row; [wideFirst] gives a pair uneven widths, null keeps it even. */
@Composable
private fun ActionRow(row: List<MenuAction>, height: Dp, wideFirst: Boolean?) {
    Row(horizontalArrangement = Arrangement.spacedBy(Gap), modifier = Modifier.fillMaxWidth().height(height)) {
        row.forEachIndexed { index, action ->
            val weight = when {
                wideFirst == null || row.size == 1 -> 1f
                (index == 0) == wideFirst -> WideWeight
                else -> 1f
            }
            ActionButton(action, weight, isHero = height == HeroHeight)
        }
    }
}

@Composable
private fun RowScope.ActionButton(action: MenuAction, baseWeight: Float, isHero: Boolean) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val motion = MaterialTheme.motionScheme
    val weight by animateFloatAsState(if (pressed) baseWeight * PressedGrowth else baseWeight, motion.fastSpatialSpec())
    val corner by animateDpAsState(
        when {
            pressed -> PressedCorner
            action.selected != null -> ToggleCorner
            else -> (if (isHero) HeroHeight else RowHeight) / 2
        },
        motion.fastSpatialSpec(),
    )
    val (targetContainer, targetContent) = colorsFor(action.emphasis, action.selected)
    val container by animateColorAsState(targetContainer, motion.defaultEffectsSpec())
    val content by animateColorAsState(targetContent, motion.defaultEffectsSpec())
    val label = action.labelArg?.let { stringResource(action.label, it) } ?: stringResource(action.label)
    Surface(
        onClick = action.onClick,
        border = if (action.selected == false) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
        interactionSource = interaction,
        shape = RoundedCornerShape(corner),
        color = container,
        contentColor = content,
        modifier = Modifier.weight(weight).fillMaxHeight().instantPress(interaction),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp),
        ) {
            if (action.emphasis == ActionEmphasis.Icon) {
                Icon(painterResource(action.icon), contentDescription = label, modifier = Modifier.size(24.dp))
            } else {
                Icon(painterResource(action.icon), null, Modifier.size(if (isHero) 24.dp else 20.dp))
                val style = if (isHero) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge
                // One line: a label that does not fit steps its size down rather than wrapping.
                BasicText(
                    text = label,
                    style = style.copy(color = LocalContentColor.current),
                    maxLines = 1,
                    softWrap = false,
                    autoSize = TextAutoSize.StepBased(minFontSize = MinLabelSize, maxFontSize = style.fontSize),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

/** Few colours, one meaning each: primary for the menu's reason, one neutral, error for removal. */
@Composable
private fun colorsFor(emphasis: ActionEmphasis, selected: Boolean?): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    if (selected != null) {
        return if (selected) scheme.primaryContainer to scheme.onPrimaryContainer else Color.Transparent to scheme.onSurfaceVariant
    }
    return when (emphasis) {
        ActionEmphasis.Primary -> scheme.primary to scheme.onPrimary
        ActionEmphasis.Secondary, ActionEmphasis.Tonal, ActionEmphasis.Icon -> scheme.surfaceContainerHighest to scheme.onSurface
        ActionEmphasis.Danger -> scheme.errorContainer to scheme.onErrorContainer
    }
}

private val HeroHeight = 72.dp
private val RowHeight = 56.dp
private val Gap = 8.dp
private val PressedCorner = 14.dp
private val ToggleCorner = 16.dp
private val MinLabelSize = 10.sp
private const val WideWeight = 1.5f
private const val PressedGrowth = 1.35f
