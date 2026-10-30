package com.wander.mono.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.ui.LauncherViewModel

/**
 * A one-time chip on home, "Swipe up for all apps", from the end of setup until the drawer is
 * first opened, however it was opened; tapping it opens the drawer too. [hidden] folds it away
 * for a while (edit mode) without counting as seen. Place it where home is always composed.
 */
@Composable
fun SwipeHint(
    modifier: Modifier = Modifier,
    hidden: Boolean = false,
    hint: SwipeHintViewModel = viewModel(),
    launcher: LauncherViewModel = viewModel(),
) {
    val shown by hint.shown.collectAsStateWithLifecycle()
    val drawerOpen by launcher.drawerOpen.collectAsStateWithLifecycle()
    LaunchedEffect(drawerOpen) {
        if (drawerOpen) hint.drawerOpened()
    }
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = shown && !hidden,
        modifier = modifier,
        enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
        exit = shrinkVertically(motion.defaultSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
    ) {
        SuggestionChip(
            onClick = { launcher.openDrawer() },
            label = { Text(stringResource(R.string.home_empty_hint)) },
            icon = {
                Icon(painterResource(R.drawable.sym_keyboard_arrow_up), contentDescription = null)
            },
            // Opaque, as live cards are: legible over any wallpaper without leaning on the scrim.
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            border = null,
            modifier = Modifier.padding(vertical = 4.dp),
        )
    }
}
