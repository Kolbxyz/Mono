package com.wander.mono.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R

/** Home with no favourite yet (spec 4.5): what home is for and the way to fill it, in the thumb zone. */
@Composable
fun EmptyHome(onChooseFavorites: () -> Unit, modifier: Modifier = Modifier) {
    val typography = MaterialTheme.typography
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier.padding(vertical = 8.dp)) {
        Text(stringResource(R.string.home_empty_title), style = typography.headlineMedium.onWallpaper())
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.home_empty_body), style = typography.bodyLarge.onWallpaper(), color = secondary)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onChooseFavorites) { Text(stringResource(R.string.home_empty_choose)) }
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.home_empty_hint), style = typography.labelLarge.onWallpaper(), color = secondary)
    }
}
