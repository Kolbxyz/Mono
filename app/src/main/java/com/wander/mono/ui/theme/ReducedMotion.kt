package com.wander.mono.ui.theme

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * Whether motion is suppressed, for the few animations that do not read `MaterialTheme.motionScheme`
 * and so are not covered by [NoMotionScheme]. Raised by the system's "Remove animations" and by battery
 * saver ([LocalPowerSave]).
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

/**
 * Accessibility's "Remove animations" sets [Settings.Global.ANIMATOR_DURATION_SCALE] to zero; that
 * is the documented signal. A [ContentObserver] keeps it live.
 */
@Composable
fun rememberSystemAnimationsDisabled(): Boolean {
    val resolver = LocalContext.current.contentResolver

    fun read() = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

    var disabled by remember { mutableStateOf(read()) }
    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                disabled = read()
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return disabled
}

/**
 * Every [MotionScheme] spec as a 1 ms tween. Not `snap()`: predictive back seeks into these specs,
 * and a snap has no curve to seek into.
 */
internal object NoMotionScheme : MotionScheme {
    private fun <T> instant(): FiniteAnimationSpec<T> = tween(durationMillis = 1)

    override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> = instant()
    override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = instant()
    override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> = instant()
    override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> = instant()
    override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = instant()
    override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> = instant()
}
