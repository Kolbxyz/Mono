package com.wander.mono.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.ui.components.SegmentOuter
import com.wander.mono.ui.home.Glance
import com.wander.mono.ui.home.GlanceContent

/**
 * The home clock as the settings below shape it, live: same face, lines and chips as home, with
 * nothing to tap. Hidden from TalkBack, which reads the settings themselves. Its sources (the
 * clock tick, battery and alarm broadcasts) answer only once registered, so the preview grows in
 * rather than appearing in one frame and pushing the rows below.
 */
@Composable
fun ClockPreviewSection(preview: ClockPreviewViewModel = viewModel()) {
    val glance by preview.glance.collectAsStateWithLifecycle()
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = glance != null,
        enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
        exit = shrinkVertically(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
    ) {
        // Never null again once read: the view model keeps its last glance.
        glance?.let { ClockPreview(it) }
    }
}

@Composable
private fun ClockPreview(shown: Glance) {
    Box(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(SegmentOuter))
            .padding(horizontal = 24.dp)
            .clearAndSetSemantics {},
    ) {
        GlanceContent(shown, taps = null, modifier = Modifier.fillMaxWidth(), compact = false)
    }
}
