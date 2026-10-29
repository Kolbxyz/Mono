package com.wander.mono.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle

/** The shadow home text drawn straight on the wallpaper gets, null when the scrim does that job. */
val LocalWallpaperTextShadow = staticCompositionLocalOf<Shadow?> { null }

/** This style with the wallpaper text shadow, when one is on. Only for text drawn on the wallpaper, never inside cards. */
@Composable
@ReadOnlyComposable
fun TextStyle.onWallpaper(): TextStyle = LocalWallpaperTextShadow.current?.let { copy(shadow = it) } ?: this
