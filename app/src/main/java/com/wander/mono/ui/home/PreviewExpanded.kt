package com.wander.mono.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.core.notifications.PreviewAction
import com.wander.mono.core.notifications.PreviewConversation
import com.wander.mono.core.notifications.PreviewExpansion
import com.wander.mono.core.notifications.Redaction
import com.wander.mono.ui.theme.secondaryTextColor

/**
 * An expanded preview row (spec 7.7): up to three conversations, each a title that opens it over
 * its last two messages, then the notification's own buttons and Mono's Mark read and Mute, all as
 * tonal chips. Content the system or the app hides says so instead of guessing.
 */
@Composable
internal fun PreviewExpanded(
    expansion: PreviewExpansion,
    onOpen: (PreviewConversation) -> Unit,
    onAction: (PreviewAction) -> Unit,
    onMarkRead: () -> Unit,
    onMute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        expansion.conversations.forEach { conversation ->
            Conversation(conversation, onOpen = { onOpen(conversation) })
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            expansion.actions.forEach { action -> TonalChip(action.label) { onAction(action) } }
            TonalChip(stringResource(R.string.preview_mark_read), onMarkRead)
            TonalChip(stringResource(R.string.preview_mute_hour), onMute)
        }
    }
}

@Composable
private fun Conversation(conversation: PreviewConversation, onOpen: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(min = MIN_TOUCH)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClickLabel = stringResource(R.string.preview_open_conversation), onClick = onOpen)
            .wrapContentHeight(Alignment.CenterVertically)
            .padding(vertical = 4.dp),
    ) {
        val hidden = conversation.redaction == Redaction.Hidden
        Text(
            conversation.title.takeUnless { hidden } ?: stringResource(R.string.preview_content_hidden),
            style = MaterialTheme.typography.titleSmall.onWallpaper(),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        conversation.messages.forEach { message ->
            Text(
                message.sender?.let { stringResource(R.string.preview_message_line, it, message.text) } ?: message.text,
                style = MaterialTheme.typography.bodyMedium.onWallpaper(),
                color = secondaryTextColor(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TonalChip(label: String, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    SuggestionChip(
        onClick = onClick,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = scheme.secondaryContainer,
            labelColor = scheme.onSecondaryContainer,
        ),
        border = null,
    )
}

private val MIN_TOUCH = 48.dp
