package com.wander.mono.ui.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.data.RecentSearch
import com.wander.mono.ui.theme.schemeAnimateItem

/**
 * Recent searches under the search field while it is [visible] (focused and empty): one chip each,
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
        Column(Modifier.padding(top = 12.dp)) {
            Text(
                stringResource(R.string.search_recent),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp).semantics { heading() },
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(recent, key = { "${it.hitKey}\n${it.query}" }) { search ->
                    RecentChip(search, onUse, onForget, schemeAnimateItem())
                }
            }
        }
    }
}

@Composable
private fun RecentChip(
    search: RecentSearch,
    onUse: (String) -> Unit,
    onForget: (RecentSearch) -> Unit,
    modifier: Modifier = Modifier,
) {
    InputChip(
        selected = false,
        onClick = { onUse(search.query) },
        label = { Text(search.query, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = {
            Icon(painterResource(R.drawable.sym_schedule), null, Modifier.size(InputChipDefaults.IconSize))
        },
        trailingIcon = {
            // Its own tap target inside the chip, which keeps its height: the cross forgets, the rest searches.
            Icon(
                painterResource(R.drawable.sym_close),
                contentDescription = stringResource(R.string.search_recent_forget, search.query),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(role = Role.Button) { onForget(search) }
                    .size(InputChipDefaults.IconSize),
            )
        },
        shape = CircleShape,
        colors = InputChipDefaults.inputChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            leadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            trailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        border = null,
        modifier = modifier,
    )
}
