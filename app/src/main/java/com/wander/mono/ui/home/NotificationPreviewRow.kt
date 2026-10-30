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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import com.wander.mono.ui.theme.isLargeFontScale
import com.wander.mono.ui.theme.secondaryTextColor
import kotlinx.coroutines.launch

/**
 * The latest notification under its favourite: "title · text" on at most two quiet lines. Tap
 * expands it to its conversations and actions (swiping its favourite right does too), a sideways
 * swipe dismisses it (when the shade would let it), and Reply opens an inline field when the
 * notification takes text. Hidden content only says there is something new. For TalkBack the
 * swipe is a Dismiss action and opening an Open action on the text, the row's focusable node.
 */
@Composable
fun NotificationPreviewRow(
    row: NotificationRow,
    modifier: Modifier = Modifier,
    previews: NotificationPreviewViewModel = viewModel(),
) {
    val replying by previews.replying.collectAsStateWithLifecycle()
    val expandedKey by previews.expanded.collectAsStateWithLifecycle()
    val expanded = replying == row.key && row.preview.reply != null
    val showsMore = expandedKey == row.key
    val scope = rememberCoroutineScope()
    // Back closes the reply first, then the expansion (spec 8).
    BackHandler(enabled = expanded) { previews.setReplying(row, open = false) }
    BackHandler(enabled = showsMore && !expanded) { previews.setExpanded(row, open = false) }

    // A newer notification starts with a fresh swipe state, even when the last one was swiped away.
    // Only the state is keyed: the content stays, so a reply being typed keeps its text and focus.
    val swipe = key(row.preview.key) { rememberSwipeToDismissBoxState() }
    val dismissLabel = stringResource(R.string.preview_dismiss)
    val openLabel = stringResource(R.string.preview_open)
    val actions = buildList {
        add(CustomAccessibilityAction(openLabel) { previews.open(row).let { true } })
        if (row.preview.isClearable) add(CustomAccessibilityAction(dismissLabel) { previews.dismiss(row) })
    }
    SwipeToDismissBox(
        state = swipe,
        backgroundContent = {},
        modifier = modifier,
        gesturesEnabled = row.preview.isClearable && !expanded,
        onDismiss = { if (!previews.dismiss(row)) scope.launch { swipe.reset() } },
    ) {
        PreviewContent(
            preview = row.preview,
            expanded = expanded,
            showsMore = showsMore,
            actions = actions,
            onToggleMore = { previews.setExpanded(row, open = !showsMore) },
            onReplyToggle = { previews.setReplying(row, open = true) },
            onSend = { previews.reply(row, it) },
        ) {
            PreviewExpanded(
                expansion = row.preview.expansion,
                onOpen = { previews.open(row, it) },
                onAction = previews::fire,
                onMarkRead = { previews.markRead(row) },
                onMute = { previews.mute(row) },
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun PreviewContent(
    preview: NotificationPreview,
    expanded: Boolean,
    showsMore: Boolean,
    actions: List<CustomAccessibilityAction>,
    onToggleMore: () -> Unit,
    onReplyToggle: () -> Unit,
    onSend: (String) -> Boolean,
    more: @Composable () -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    val stacked = isLargeFontScale()
    val toggleLabel = stringResource(if (showsMore) R.string.preview_collapse else R.string.preview_expand)
    Column(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                previewText(preview),
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryTextColor(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = MIN_TOUCH)
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClickLabel = toggleLabel, onClick = onToggleMore)
                    .semantics { customActions = actions }
                    .wrapContentHeight(Alignment.CenterVertically)
                    .padding(vertical = 4.dp),
            )
            if (!stacked) ReplyButton(preview, expanded, onReplyToggle)
        }
        if (stacked) ReplyButton(preview, expanded, onReplyToggle, Modifier.align(Alignment.End))
        AnimatedVisibility(
            visible = showsMore,
            enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.fastEffectsSpec()),
            exit = shrinkVertically(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
        ) {
            more()
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
private fun ReplyButton(
    preview: NotificationPreview,
    expanded: Boolean,
    onReplyToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reply = preview.reply?.takeIf { !expanded } ?: return
    TextButton(onClick = onReplyToggle, modifier = modifier) {
        Text(reply.label.ifBlank { stringResource(R.string.preview_reply) })
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

private val MIN_TOUCH = 48.dp
