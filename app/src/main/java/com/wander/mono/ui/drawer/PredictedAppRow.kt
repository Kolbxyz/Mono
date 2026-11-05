package com.wander.mono.ui.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.apps.LaunchableApp
import com.wander.mono.ui.components.AppIcons
import com.wander.mono.ui.home.onWallpaper

/**
 * The app most likely wanted now ([AppPrediction]), right above the search field where the thumb
 * rests, straight on the drawer's background like every other row, under its heading. Unfolds when there is one and folds away when
 * there is none, keeping the last app on screen while it leaves.
 */
@Composable
internal fun PredictedAppRow(
    entry: DrawerEntry?,
    icons: AppIcons,
    onLaunch: (LaunchableApp) -> Unit,
    onLongPress: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Not snapshot state: only what this composition already reads, read right here.
    val last = remember { LastPredicted() }
    if (entry != null) last.entry = entry
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = entry != null,
        modifier = modifier,
        enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
        exit = shrinkVertically(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
    ) {
        val app = last.entry ?: return@AnimatedVisibility
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
            Text(
                stringResource(R.string.drawer_predicted),
                style = MaterialTheme.typography.labelLarge.onWallpaper(),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp).semantics { heading() },
            )
            DrawerAppRow(app, icons, onLaunch = { onLaunch(app.app) }, onLongPress = { onLongPress(app.app) })
        }
    }
}

private class LastPredicted(var entry: DrawerEntry? = null)
