package com.wander.mono.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.core.notifications.NotificationPreview
import com.wander.mono.core.notifications.Redaction
import kotlinx.coroutines.launch

/**
 * The latest notification under its favourite: "title · text" on at most two quiet lines. Tap
 * opens it, a sideways swipe dismisses it (when the shade would let it), and Reply opens an inline
 * field when the notification takes text. Hidden content only says there is something new.
 */
@Composable
fun NotificationPreviewRow(
    row: NotificationRow,
    modifier: Modifier = Modifier,
    previews: NotificationPreviewViewModel = viewModel(),
) {
    val replying by previews.replying.collectAsStateWithLifecycle()
    val expanded = replying == row.key && row.preview.reply != null
    val scope = rememberCoroutineScope()
    BackHandler(enabled = expanded) { previews.setReplying(row, open = false) }

    // A newer notification starts with a fresh swipe state, even when the last one was swiped away.
    key(row.preview.key) {
        val swipe = rememberSwipeToDismissBoxState()
        val dismissLabel = stringResource(R.string.preview_dismiss)
        SwipeToDismissBox(
            state = swipe,
            backgroundContent = {},
            modifier = modifier.semantics {
                if (row.preview.isClearable) {
                    customActions = listOf(CustomAccessibilityAction(dismissLabel) { previews.dismiss(row) })
                }
            },
            gesturesEnabled = row.preview.isClearable && !expanded,
            onDismiss = { if (!previews.dismiss(row)) scope.launch { swipe.reset() } },
        ) {
            PreviewContent(
                preview = row.preview,
                expanded = expanded,
                onOpen = { previews.open(row) },
                onReplyToggle = { previews.setReplying(row, open = true) },
                onSend = { previews.reply(row, it) },
            )
        }
    }
}

@Composable
private fun PreviewContent(
    preview: NotificationPreview,
    expanded: Boolean,
    onOpen: () -> Unit,
    onReplyToggle: () -> Unit,
    onSend: (String) -> Boolean,
) {
    val motion = MaterialTheme.motionScheme
    Column(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                previewText(preview),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClickLabel = stringResource(R.string.preview_open), onClick = onOpen)
                    .padding(vertical = 4.dp),
            )
            preview.reply?.takeIf { !expanded }?.let { reply ->
                TextButton(onClick = onReplyToggle) {
                    Text(reply.label.ifBlank { stringResource(R.string.preview_reply) })
                }
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
            exit = shrinkVertically(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
        ) {
            ReplyField(hint = preview.reply?.hint, onSend = onSend, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun previewText(preview: NotificationPreview) = buildAnnotatedString {
    if (preview.redaction == Redaction.Hidden) {
        append(stringResource(R.string.preview_hidden))
        return@buildAnnotatedString
    }
    preview.title?.let { title ->
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) { append(title) }
        if (preview.text != null) append(" · ")
    }
    preview.text?.let(::append)
}
