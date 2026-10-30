package com.wander.mono.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.wander.mono.R
import com.wander.mono.core.notifications.NotificationPreview
import com.wander.mono.core.notifications.Redaction
import com.wander.mono.ui.theme.secondaryTextColor

/**
 * An app's latest notification as one quiet line under its name, Niagara style: "Title · text",
 * cut short with an ellipsis. Hidden content only says there is something new. It sets small body
 * type over the surrounding [LocalTextStyle], so a caller can add a shadow (home on the wallpaper).
 */
@Composable
fun PreviewLine(preview: NotificationPreview, modifier: Modifier = Modifier) {
    Text(
        previewText(preview),
        modifier = modifier,
        style = LocalTextStyle.current.merge(MaterialTheme.typography.bodySmall),
        color = secondaryTextColor(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** "Title · text", the title in the full text colour; for hidden content, that there is something new. */
@Composable
internal fun previewText(preview: NotificationPreview): AnnotatedString = buildAnnotatedString {
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
