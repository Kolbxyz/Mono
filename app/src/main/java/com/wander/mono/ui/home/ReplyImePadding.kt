package com.wander.mono.ui.home

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.flow.first

/**
 * Room for the keyboard under home's content while an inline reply is open, and only then: the
 * drawer's search keyboard must not move home behind it. After the reply closes the room stays
 * until the keyboard is gone, so the list follows it down instead of dropping at once. Insets
 * already padded above (the navigation bar) are not counted twice.
 */
@Composable
internal fun replyImePadding(replyOpen: Boolean): Modifier {
    val ime = WindowInsets.ime
    val density = LocalDensity.current
    // Set while a reply is open and kept past its closing until the keyboard is down.
    var holding by remember { mutableStateOf(false) }
    LaunchedEffect(replyOpen) {
        if (replyOpen) {
            holding = true
        } else {
            snapshotFlow { ime.getBottom(density) == 0 }.first { it }
            holding = false
        }
    }
    return if (replyOpen || holding) Modifier.windowInsetsPadding(ime.only(WindowInsetsSides.Bottom)) else Modifier
}
