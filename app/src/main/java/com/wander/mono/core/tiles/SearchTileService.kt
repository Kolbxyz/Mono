package com.wander.mono.core.tiles

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.wander.mono.core.tiles.TileRequest.Companion.openMono

/** The Search tile: a tap opens Mono's drawer with the search field focused and the keyboard up. */
class SearchTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        // An action, not a switch: always drawn off, like the system's own action tiles.
        tile.state = Tile.STATE_INACTIVE
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        openMono(TileRequest.Search)
    }
}
