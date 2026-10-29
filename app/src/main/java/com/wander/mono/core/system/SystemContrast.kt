package com.wander.mono.core.system

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityManager
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf

/**
 * Whether Android asks apps for more contrast: the accessibility "High contrast text" switch
 * (readable from Android 16) or a raised contrast level in Display settings (Android 14). Both
 * come from the platform's change listeners; older versions simply never ask.
 */
@Singleton
class SystemContrast @Inject constructor(@ApplicationContext private val context: Context) {
    val raised: Flow<Boolean> = combine(highContrastText(), contrastLevel()) { text, level ->
        text || isRaisedContrast(level)
    }.distinctUntilChanged()

    private fun highContrastText(): Flow<Boolean> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) observeHighContrastText() else flowOf(false)

    private fun contrastLevel(): Flow<Float> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) observeContrastLevel() else flowOf(0f)

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    private fun observeHighContrastText(): Flow<Boolean> = callbackFlow {
        val manager = context.getSystemService(AccessibilityManager::class.java)
        val listener = AccessibilityManager.HighContrastTextStateChangeListener { trySend(it) }
        trySend(manager.isHighContrastTextEnabled)
        manager.addHighContrastTextStateChangeListener(context.mainExecutor, listener)
        awaitClose { manager.removeHighContrastTextStateChangeListener(listener) }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun observeContrastLevel(): Flow<Float> = callbackFlow {
        val manager = context.getSystemService(UiModeManager::class.java)
        val listener = UiModeManager.ContrastChangeListener { trySend(it) }
        trySend(manager.contrast)
        manager.addContrastChangeListener(context.mainExecutor, listener)
        awaitClose { manager.removeContrastChangeListener(listener) }
    }
}

/**
 * [UiModeManager.getContrast] runs from -1 (reduced) through 0 (standard) to 1; Display settings
 * offers medium (0.5) and high (1). Either is a request for more contrast.
 */
internal fun isRaisedContrast(level: Float): Boolean = level >= MEDIUM_CONTRAST

private const val MEDIUM_CONTRAST = 0.5f
