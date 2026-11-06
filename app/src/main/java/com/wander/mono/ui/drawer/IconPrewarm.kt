package com.wander.mono.ui.drawer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wander.mono.data.settings.IconMode
import com.wander.mono.ui.components.AppIcons

/**
 * Loads the icons of every app in [groups] into memory at a drawer row's [size], in the background,
 * once per change of the list: a letter held on home's rail, or the drawer opening at one, then
 * finds its icons there and draws them in its first frame instead of fading them in as they decode.
 * Bounded by the icon cache's own size; nothing in text-only mode.
 */
@Composable
internal fun PrewarmIcons(groups: List<AppGroup>, icons: AppIcons, size: Dp = ROW_ICON) {
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    LaunchedEffect(groups, icons, sizePx) {
        if (icons.mode == IconMode.TextOnly) return@LaunchedEffect
        groups.forEach { group ->
            group.entries.forEach { entry ->
                if (icons.peek(entry.app.key, icons.mode, sizePx) == null) icons.load(entry.app.key, icons.mode, sizePx)
            }
        }
    }
}

/** The icon size of a drawer row ([DrawerAppRow]'s default). */
private val ROW_ICON = 40.dp
