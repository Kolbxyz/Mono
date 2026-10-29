package com.wander.mono.ui.components

import androidx.activity.compose.PredictiveBackHandler
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/** How small a sheet gets at the far end of a back swipe, before it commits and slides away. */
private const val BackMinScale = 0.9f

/**
 * Every bottom sheet in Mono: a [ModalBottomSheet] that shrinks toward its foot on a back swipe
 * instead of the stock plain slide.
 *
 * [content] receives `animatedDismiss`: an action should call it instead of [onDismissRequest], so
 * the sheet runs its hide animation before the caller's state drops it from composition.
 */
@Composable
fun MonoSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.(animatedDismiss: () -> Unit) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var backProgress by remember { mutableFloatStateOf(0f) }
    val animatedDismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismissRequest() }
    }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false),
        modifier = modifier.graphicsLayer {
            val scale = lerp(1f, BackMinScale, backResistance(backProgress))
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(0.5f, 1f)
        },
    ) {
        PredictiveBackHandler(enabled = true) { progress ->
            try {
                progress.collect { backProgress = it.progress }
                animatedDismiss()
            } catch (_: CancellationException) {
                backProgress = 0f
            }
        }
        // No stretch overscroll inside a sheet, so the first downward drag moves the sheet itself.
        CompositionLocalProvider(LocalOverscrollFactory provides null) {
            Column(Modifier.fillMaxWidth()) { content(animatedDismiss) }
        }
    }
}
