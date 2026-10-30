package com.wander.mono.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.core.system.BundledWallpaper
import com.wander.mono.core.system.WallpaperTarget
import com.wander.mono.ui.components.MonoSheet
import com.wander.mono.ui.components.PressMorph
import com.wander.mono.ui.components.SegmentedColumn
import com.wander.mono.ui.components.expressivePress
import com.wander.mono.ui.components.instantPress

/**
 * Mono's bundled wallpapers as a row of previews; a tap asks where it goes (home, lock or both
 * screens) and sets it, with a snackbar for the outcome.
 */
@Composable
fun WallpapersSection(looks: LooksViewModel = viewModel()) {
    var picked by rememberSaveable { mutableStateOf<BundledWallpaper?>(null) }
    SettingsGroup(R.string.wallpapers) {
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(BundledWallpaper.entries) { wallpaper -> WallpaperTile(wallpaper) { picked = wallpaper } }
        }
    }
    picked?.let { wallpaper ->
        WallpaperTargetSheet(onDismiss = { picked = null }) { target -> looks.setWallpaper(wallpaper, target) }
    }
}

@Composable
private fun WallpaperTile(wallpaper: BundledWallpaper, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .expressivePress(interaction, PressMorph.squarer(TileCorner))
            .instantPress(interaction)
            .clickable(interaction, ripple(), role = Role.Button, onClick = onClick),
    ) {
        Box(
            Modifier
                .size(TileWidth, TileHeight)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            WallpaperThumbnail(wallpaper.image, TileWidth, Modifier.fillMaxSize())
        }
        Text(
            stringResource(wallpaper.label),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }
}

/** Where the picked wallpaper goes; the sheet slides away before it is set. */
@Composable
private fun WallpaperTargetSheet(onDismiss: () -> Unit, onPick: (WallpaperTarget) -> Unit) {
    MonoSheet(onDismissRequest = onDismiss) { dismiss ->
        Text(
            stringResource(R.string.wallpaper_set_on),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        SegmentedColumn(Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)) {
            WallpaperTarget.entries.forEach { target ->
                ListItem(
                    colors = settingRowColors(),
                    modifier = Modifier.clickable(role = Role.Button) { dismiss { onPick(target) } },
                ) {
                    Text(stringResource(target.label()))
                }
            }
        }
    }
}

private fun WallpaperTarget.label(): Int = when (this) {
    WallpaperTarget.Home -> R.string.wallpaper_target_home
    WallpaperTarget.Lock -> R.string.wallpaper_target_lock
    WallpaperTarget.Both -> R.string.wallpaper_target_both
}

/** A phone's shape, small enough that three and a bit fit across. */
private val TileWidth = 96.dp
private val TileHeight = 200.dp
private val TileCorner = 26.dp
