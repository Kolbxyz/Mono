package com.wander.mono.ui.settings

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wander.mono.core.apps.CatalogChange
import com.wander.mono.core.apps.CatalogEvents
import com.wander.mono.core.apps.IconPacks
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/** An icon pack as the picker lists it: its name and its own icon. */
class IconPackChoice(val packageName: String, val label: String, val icon: ImageBitmap?)

/** The installed icon packs, listed again when a package comes or goes; null until first read. */
@HiltViewModel
class IconPackViewModel @Inject constructor(packs: IconPacks, events: CatalogEvents) : ViewModel() {
    val packs: StateFlow<List<IconPackChoice>?> = events.changes
        .filter { it is CatalogChange.Packages }
        .map { }
        .onStart { emit(Unit) }
        .map {
            packs.installed().map { pack ->
                val icon = packs.packIcon(pack.packageName)?.toBitmap(ICON_PX, ICON_PX)?.asImageBitmap()
                IconPackChoice(pack.packageName, pack.label, icon)
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L

        /** Drawn at 40 dp; this covers xxxhdpi. */
        const val ICON_PX = 160
    }
}
