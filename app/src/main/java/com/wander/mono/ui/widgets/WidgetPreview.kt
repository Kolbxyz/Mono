package com.wander.mono.ui.widgets

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** What the picker can show for a widget, cheapest real picture first. */
private sealed interface Preview {
    data class Picture(val image: ImageBitmap) : Preview

    /** Android 12+ preview layout, inflated from the provider's own resources. */
    data object Layout : Preview

    data class Icon(val image: ImageBitmap) : Preview

    /** Not read yet: the entry keeps a preview's room, so the list does not jump when it arrives. */
    data object Loading : Preview
}

/**
 * The provider's preview image, else (Android 12+, main profile) its preview layout, else its icon.
 * Empty room the provider's size while loading; nothing at all when the provider has none of these.
 */
@Composable
internal fun WidgetPreview(
    info: AppWidgetProviderInfo,
    isMainProfile: Boolean,
    createPreview: (Context, AppWidgetProviderInfo) -> View,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val preview by produceState<Preview?>(Preview.Loading, info) {
        value = withContext(Dispatchers.IO) { loadPreview(context, info, isMainProfile) }
    }
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        when (val shown = preview) {
            is Preview.Picture -> Image(
                shown.image,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.heightIn(max = MAX_PREVIEW_HEIGHT),
            )
            Preview.Layout -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AndroidView(
                    factory = { createPreview(it, info) },
                    modifier = Modifier.fillMaxWidth().height(layoutHeight(context, info)),
                )
            }
            is Preview.Icon -> Image(shown.image, contentDescription = null, modifier = Modifier.size(ICON_SIZE))
            Preview.Loading -> Spacer(Modifier.height(layoutHeight(context, info)))
            null -> Unit
        }
    }
}

private fun loadPreview(context: Context, info: AppWidgetProviderInfo, isMainProfile: Boolean): Preview? {
    val maxPx = (MAX_PREVIEW_HEIGHT.value * context.resources.displayMetrics.density * 2).toInt()
    val picture = info.loadPreviewImage(context, 0)
    // A preview layout is inflated with the package's resources as Mono's own profile sees them.
    val hasLayout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && info.previewLayout != 0 && isMainProfile
    return when {
        picture != null -> Preview.Picture(picture.scaledBitmap(maxPx))
        hasLayout -> Preview.Layout
        else -> info.loadIcon(context, context.resources.displayMetrics.densityDpi)?.let {
            Preview.Icon(it.scaledBitmap(maxPx))
        }
    }
}

/** Large preview images are scaled down to [maxPx] on their longer side before they are kept. */
private fun Drawable.scaledBitmap(maxPx: Int): ImageBitmap {
    val width = intrinsicWidth.takeIf { it > 0 } ?: maxPx
    val height = intrinsicHeight.takeIf { it > 0 } ?: maxPx
    val scale = minOf(1f, maxPx.toFloat() / maxOf(width, height))
    return toBitmap((width * scale).toInt().coerceAtLeast(1), (height * scale).toInt().coerceAtLeast(1))
        .asImageBitmap()
}

/** The provider's minimum height, kept within what fits a picker entry. */
private fun layoutHeight(context: Context, info: AppWidgetProviderInfo) =
    (info.minHeight / context.resources.displayMetrics.density)
        .coerceIn(MIN_LAYOUT_HEIGHT.value, MAX_PREVIEW_HEIGHT.value).dp

private val MAX_PREVIEW_HEIGHT = 160.dp
private val MIN_LAYOUT_HEIGHT = 72.dp
private val ICON_SIZE = 48.dp
