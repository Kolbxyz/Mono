package com.wander.mono.ui.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.core.apps.AppIconImage
import com.wander.mono.core.apps.AppShortcut
import com.wander.mono.data.settings.IconMode
import com.wander.mono.ui.components.AppIcon
import com.wander.mono.ui.components.AppIcons

/** Loads a shortcut's own icon at a pixel size; null when it has none. */
typealias ShortcutIconLoader = suspend (AppShortcut, Int) -> AppIconImage?

/** The drawer's shortcut icon loader, provided by [DrawerScreen] for every row under it. */
internal val LocalShortcutIcons = staticCompositionLocalOf<ShortcutIconLoader> {
    error("ShortcutIcon used outside DrawerScreen, which provides the loader")
}

/**
 * A shortcut's own icon (a contact's face, a chat's avatar) with its app's icon as a small badge at
 * the bottom end, ringed in [ring], the colour the row sits on. A shortcut without an icon of its own
 * shows its app's icon alone. Nothing in text-only mode. Decorative: the row's text names both.
 */
@Composable
internal fun ShortcutIcon(
    hit: SearchHit.Shortcut,
    icons: AppIcons,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    ring: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
    if (icons.mode == IconMode.TextOnly) return
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    val loadShortcutIcon = LocalShortcutIcons.current
    val own by produceState<AppIconImage?>(null, hit.shortcut, sizePx) {
        value = loadShortcutIcon(hit.shortcut, sizePx)
    }
    val image = own
    if (image == null) {
        AppIcon(hit.owner?.key, icons, modifier, size)
        return
    }
    Box(modifier.size(size)) {
        Image(image.bitmap, contentDescription = null, modifier = Modifier.size(size).clip(CircleShape))
        val badge = size * BADGE_FRACTION
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .size(badge)
                .background(ring, CircleShape)
                .padding(BADGE_RING),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(hit.owner?.key, icons, size = badge - BADGE_RING * 2)
        }
    }
}

/** The app badge's size against the shortcut icon's. */
private const val BADGE_FRACTION = 0.45f

/** The ring that cuts the badge out of the shortcut icon under it. */
private val BADGE_RING = 2.dp
