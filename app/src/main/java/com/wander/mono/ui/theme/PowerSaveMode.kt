package com.wander.mono.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wander.mono.core.system.isPowerSaveMode
import com.wander.mono.core.system.powerSaveMode

/**
 * Whether battery saver is on. [MonoTheme] provides it; while on, Mono drops the window blur and
 * every decorative motion ([LocalReducedMotion] is raised with it), keeping only the motion that
 * says where things went.
 */
val LocalPowerSave = staticCompositionLocalOf { false }

/** Battery saver, live from its broadcast while the activity is started. */
@Composable
internal fun rememberPowerSaveMode(): Boolean {
    val context = LocalContext.current.applicationContext
    val flow = remember(context) { powerSaveMode(context) }
    val initial = remember(context) { isPowerSaveMode(context) }
    return flow.collectAsStateWithLifecycle(initial).value
}

/**
 * The motion for what only decorates (the clock's weight through the day, the player's tint): the
 * theme's own, or [NoMotionScheme] while [LocalReducedMotion] is raised, by the system or by
 * battery saver.
 */
@Composable
@ReadOnlyComposable
fun decorativeMotion(): MotionScheme = if (LocalReducedMotion.current) NoMotionScheme else MaterialTheme.motionScheme
