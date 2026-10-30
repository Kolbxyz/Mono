package com.wander.mono.ui.drawer

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.AppCategory
import com.wander.mono.ui.theme.schemeAnimateItem

/**
 * The categories with apps in the list, as one scrolling row of tonal filter chips, unfolding under
 * the search field while [visible]. Tapping the [selected] chip again shows every category.
 */
@Composable
internal fun CategoryChips(
    visible: Boolean,
    categories: List<AppCategory>,
    selected: AppCategory?,
    onSelect: (AppCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = visible && categories.isNotEmpty(),
        modifier = modifier,
        enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
        exit = shrinkVertically(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
    ) {
        LazyRow(
            Modifier.fillMaxWidth().padding(bottom = 8.dp).selectableGroup(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories, key = { it.name }) { category ->
                val chosen = category == selected
                FilterChip(
                    selected = chosen,
                    onClick = { onSelect(if (chosen) null else category) },
                    label = { Text(stringResource(categoryLabel(category))) },
                    modifier = schemeAnimateItem(),
                    leadingIcon = if (chosen) {
                        {
                            val size = Modifier.size(FilterChipDefaults.IconSize)
                            Icon(painterResource(R.drawable.sym_check), contentDescription = null, modifier = size)
                        }
                    } else {
                        null
                    },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    // Tonal pills, like the search history's: the fill tells chosen from not, no outline needed.
                    border = null,
                )
            }
        }
    }
}

@StringRes
private fun categoryLabel(category: AppCategory): Int = when (category) {
    AppCategory.Games -> R.string.category_games
    AppCategory.Audio -> R.string.category_audio
    AppCategory.Video -> R.string.category_video
    AppCategory.Image -> R.string.category_image
    AppCategory.Social -> R.string.category_social
    AppCategory.News -> R.string.category_news
    AppCategory.Maps -> R.string.category_maps
    AppCategory.Productivity -> R.string.category_productivity
    AppCategory.Accessibility -> R.string.category_accessibility
    AppCategory.Other -> R.string.category_other
}
