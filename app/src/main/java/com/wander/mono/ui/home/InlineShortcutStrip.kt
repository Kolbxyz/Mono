package com.wander.mono.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.data.settings.IconMode
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.sheet.ShortcutRow

/**
 * [app]'s shortcuts unfolded under its favourite by a swipe right: small chips in a row that
 * scrolls sideways, lined up with the favourite's name and growing out from under it on the motion
 * scheme. A tap launches one, through the app lock; TalkBack hears the row as a pane.
 */
@Composable
internal fun InlineShortcutStrip(inline: InlineShortcuts, app: LaunchableApp, icons: AppIcons) {
    val motion = MaterialTheme.motionScheme
    val start = favoriteRowMetrics(LocalRowDensity.current).labelStart(icons.mode != IconMode.TextOnly)
    val pane = stringResource(R.string.inline_shortcuts_label, app.label)
    AnimatedVisibility(
        visible = inline.isOpen(app.key),
        enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
        exit = shrinkVertically(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
    ) {
        ShortcutRow(
            app = app.key,
            shortcuts = inline.shortcutsOf(app.key),
            icons = icons,
            loadIcon = { shortcut, sizePx -> inline.model.icon(shortcut, sizePx) },
            onShortcut = inline.model::launch,
            modifier = Modifier
                .padding(bottom = StripGap)
                .semantics { paneTitle = pane },
            small = true,
            contentPadding = PaddingValues(start = start, end = FavoriteRowInset),
        )
    }
}

/** The room under the chips before the next row. */
private val StripGap = 8.dp
