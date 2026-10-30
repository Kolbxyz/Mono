package com.wander.mono.ui.sheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R

/**
 * The small lock after a locked app's name in its sheet, the only place a lock shows: home and the
 * drawer stay as they are. It pops in and out as the lock is switched.
 */
@Composable
internal fun LockBadge(locked: Boolean) {
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = locked,
        enter = scaleIn(motion.fastSpatialSpec()) + fadeIn(motion.fastEffectsSpec()),
        exit = scaleOut(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Icon(
                painterResource(R.drawable.sym_lock),
                contentDescription = stringResource(R.string.sheet_locked),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(4.dp).size(14.dp),
            )
        }
    }
}
