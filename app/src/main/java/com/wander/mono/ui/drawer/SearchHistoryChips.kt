package com.wander.mono.ui.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.data.RecentSearch
import com.wander.mono.ui.theme.schemeAnimateItem

/**
 * Recent searches just above the search field while it is [visible] (focused and empty): one chip each,
 * newest first, scrolling sideways. Tapping a chip types its query again; its cross forgets it.
 */
@Composable
internal fun RecentSearchChips(
    visible: Boolean,
    recent: List<RecentSearch>,
    onUse: (String) -> Unit,
    onForget: (RecentSearch) -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = visible && recent.isNotEmpty(),
        enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
        exit = shrinkVertically(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
    ) {
        Column(Modifier.padding(bottom = 8.dp)) {
            Text(
                stringResource(R.string.search_recent),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp).semantics { heading() },
            )
            // Lined up with the field's edge; the gaps leave room for each cross's touch target.
            LazyRow(
                contentPadding = PaddingValues(end = CHIP_GAP),
                horizontalArrangement = Arrangement.spacedBy(CHIP_GAP),
            ) {
                items(recent, key = { "${it.hitKey}\n${it.query}" }) { search ->
                    RecentChip(search, onUse, onForget, schemeAnimateItem())
                }
            }
        }
    }
}

/**
 * One remembered search on the field's own tone. The cross is a separate 48 dp target laid over the
 * chip's end, centred on the glyph, so the chip keeps its height and a thumb still finds it.
 */
@Composable
private fun RecentChip(
    search: RecentSearch,
    onUse: (String) -> Unit,
    onForget: (RecentSearch) -> Unit,
    modifier: Modifier = Modifier,
) {
    val forget = stringResource(R.string.search_recent_forget, search.query)
    Box(modifier, contentAlignment = Alignment.CenterEnd) {
        InputChip(
            selected = false,
            onClick = { onUse(search.query) },
            label = { Text(search.query, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            leadingIcon = {
                Icon(painterResource(R.drawable.sym_schedule), null, Modifier.size(InputChipDefaults.IconSize))
            },
            trailingIcon = {
                Icon(painterResource(R.drawable.sym_close), null, Modifier.size(InputChipDefaults.IconSize))
            },
            shape = CircleShape,
            colors = InputChipDefaults.inputChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                labelColor = MaterialTheme.colorScheme.onSurface,
                leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            border = null,
        )
        Box(
            Modifier
                .offset(x = ChipCross.shift(InputChipDefaults.IconSize.value).dp)
                .size(ChipCross.TARGET.dp)
                .clickable(
                    interactionSource = null,
                    indication = ripple(bounded = false, radius = InputChipDefaults.Height / 2),
                    role = Role.Button,
                ) { onForget(search) }
                .semantics { contentDescription = forget },
        )
    }
}

/** Between chips, room for a cross's target reaching past its chip's end. */
private val CHIP_GAP = ChipCross.GAP.dp

/** Where a history chip's forget target sits, in dp: a full touch target centred on the cross. */
internal object ChipCross {
    /** A Material touch target. */
    const val TARGET = 48f

    /** The chip's end padding beside a trailing icon. */
    const val END_PADDING = 8f

    /** The gap between chips, which the target may reach into. */
    const val GAP = 8f

    /** How far the target, laid at the chip's end, moves past it to centre on a cross [icon] wide. */
    fun shift(icon: Float): Float = TARGET / 2 - (END_PADDING + icon / 2)
}
