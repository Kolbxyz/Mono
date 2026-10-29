package com.wander.mono.ui.drawer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.data.settings.IconMode
import com.wander.mono.ui.components.AppIcons

/** The calculator's answer, large; tapping copies it. */
@Composable
fun CalculationRow(hit: SearchHit.Calculation, icons: AppIcons, onCopy: () -> Unit, modifier: Modifier = Modifier) {
    SymbolRow(
        symbol = R.drawable.sym_calculate,
        title = stringResource(R.string.search_calculator_result, hit.result),
        subtitle = stringResource(R.string.search_calculator_copy),
        clickLabel = R.string.action_copy,
        icons = icons,
        onClick = onCopy,
        modifier = modifier,
        titleStyle = MaterialTheme.typography.headlineSmall,
    )
}

@Composable
fun PersonRow(hit: SearchHit.Person, icons: AppIcons, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    SymbolRow(
        symbol = R.drawable.sym_person,
        title = hit.label,
        subtitle = stringResource(R.string.search_contact),
        clickLabel = R.string.action_open,
        icons = icons,
        onClick = onOpen,
        modifier = modifier,
    )
}

/** A song in Wanda; the subtitle says so, with the artist when Wanda knows it. */
@Composable
fun TrackRow(hit: SearchHit.Track, icons: AppIcons, onPlay: () -> Unit, modifier: Modifier = Modifier) {
    val kind = stringResource(R.string.search_track)
    SymbolRow(
        symbol = R.drawable.sym_music_note,
        title = hit.label,
        subtitle = hit.track.artist?.let { stringResource(R.string.search_track_by, kind, it) } ?: kind,
        clickLabel = R.string.media_play,
        icons = icons,
        onClick = onPlay,
        modifier = modifier,
    )
}

@Composable
fun SettingRow(hit: SearchHit.Setting, icons: AppIcons, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    SymbolRow(
        symbol = R.drawable.sym_settings,
        title = hit.label,
        subtitle = stringResource(R.string.settings_title),
        clickLabel = R.string.action_open,
        icons = icons,
        onClick = onOpen,
        modifier = modifier,
    )
}

/** The fallback when nothing matched: hand the query to the browser. */
@Composable
fun WebRow(hit: SearchHit.Web, icons: AppIcons, onSearch: () -> Unit, modifier: Modifier = Modifier) {
    SymbolRow(
        symbol = R.drawable.sym_search,
        title = hit.appLabel?.let { stringResource(R.string.search_web_in, it) }
            ?: stringResource(R.string.search_web),
        subtitle = hit.query,
        clickLabel = R.string.action_search,
        icons = icons,
        onClick = onSearch,
        modifier = modifier,
    )
}

/**
 * A result that is not an app: a symbol where app rows have their icon (none in text-only mode,
 * like apps), a title and what kind of result it is underneath.
 */
@Composable
private fun SymbolRow(
    @DrawableRes symbol: Int,
    title: String,
    subtitle: String,
    @StringRes clickLabel: Int,
    icons: AppIcons,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = MIN_ROW_HEIGHT)
            .clickable(onClickLabel = stringResource(clickLabel), onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icons.mode != IconMode.TextOnly) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(symbol),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column {
            Text(title, style = titleStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
