package com.wander.mono.ui.drawer

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wander.mono.ui.components.PressMorph
import com.wander.mono.ui.components.expressivePress
import com.wander.mono.ui.components.instantPress

/**
 * A drawer or search row's tap, and long press given [onLongClick]: Mono's [expressivePress] shrink,
 * shown the moment the finger lands as the rows sit in scrolling lists, with the ripple in [accent]
 * (the app's colour, when rows are tinted) or the theme's. The row is clipped to a rounded outline
 * that tightens a little under the finger, so the ripple shows as a rounded highlight whether the row
 * sits straight on the drawer's background or in a search result's card (which clips it again).
 */
@Composable
internal fun Modifier.rowPress(
    onClickLabel: String,
    onClick: () -> Unit,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    accent: Color? = null,
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return expressivePress(interaction, ROW_MORPH)
        .instantPress(interaction)
        .combinedClickable(
            interactionSource = interaction,
            indication = ripple(color = accent ?: Color.Unspecified),
            onClickLabel = onClickLabel,
            onLongClickLabel = onLongClickLabel,
            onLongClick = onLongClick,
            onClick = onClick,
        )
}

/** A row's pressed highlight: the theme's large corner (26 dp), slightly squarer under the finger. */
private val ROW_MORPH = PressMorph.squarer(26.dp)
