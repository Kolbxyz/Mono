package com.wander.mono.ui.components

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/** How small a sheet gets at the far end of a back swipe, before it commits and slides away. */
private const val BACK_MIN_SCALE = 0.9f

/** The middle of the sheet's bottom edge, which a back swipe shrinks it toward. */
private val SheetFoot = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 1f)

/**
 * Slides a [MonoSheet] away, then calls its `onDismissRequest` and the `then` given to [invoke],
 * so the caller's state only drops the sheet once it is off screen.
 */
@Stable
class SheetDismiss internal constructor(
    private val scope: CoroutineScope,
    private val sheetState: SheetState,
    private val onDismissRequest: () -> Unit,
) {
    operator fun invoke(then: () -> Unit = {}) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { cause ->
            onDismissRequest()
            // Cancelled: the sheet already left with something else (home pressed), which the action must not undo.
            if (cause == null) then()
        }
    }
}

/**
 * Every bottom sheet in Mono: a [ModalBottomSheet] that shrinks toward its foot on a back swipe
 * instead of the stock plain slide.
 *
 * [content] receives `animatedDismiss`: an action should call it instead of [onDismissRequest], so
 * the sheet runs its hide animation before the caller's state drops it from composition. An action
 * whose caller drops the sheet the moment it runs goes in its `then`, to run once the sheet is away.
 */
@Composable
fun MonoSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.(animatedDismiss: SheetDismiss) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val backProgress = remember { Animatable(0f) }
    val cancelSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)
    val animatedDismiss = remember(scope, sheetState) { SheetDismiss(scope, sheetState) { currentOnDismiss() } }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false),
        modifier = modifier.graphicsLayer {
            val scale = lerp(1f, BACK_MIN_SCALE, backResistance(backProgress.value))
            scaleX = scale
            scaleY = scale
            transformOrigin = SheetFoot
        },
    ) {
        PredictiveBackHandler(enabled = true) { progress ->
            try {
                progress.collect { backProgress.snapTo(it.progress) }
                animatedDismiss()
            } catch (_: CancellationException) {
                // This coroutine is already cancelled, so the settle back to full size runs on the sheet's.
                scope.launch { backProgress.animateTo(0f, cancelSpec) }
            }
        }
        // No stretch overscroll inside a sheet, so the first downward drag moves the sheet itself.
        CompositionLocalProvider(LocalOverscrollFactory provides null) {
            Column(Modifier.fillMaxWidth()) { content(animatedDismiss) }
        }
    }
}
