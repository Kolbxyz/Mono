package com.wander.mono.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Takes the full width and centres what follows in a column no wider than
 * [ReadableLayout.MaxColumnWidth]; on a phone in portrait it changes nothing. Put the container's
 * horizontal `safeDrawing` padding before it, so a landscape cutout only pushes the column when
 * the column actually reaches it.
 */
fun Modifier.readableColumn(): Modifier = this
    .fillMaxWidth()
    .wrapContentWidth(Alignment.CenterHorizontally)
    .widthIn(max = ReadableLayout.MaxColumnWidth)
