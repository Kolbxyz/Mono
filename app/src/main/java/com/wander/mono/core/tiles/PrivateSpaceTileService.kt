package com.wander.mono.core.tiles

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.wander.mono.R
import com.wander.mono.core.apps.PrivateSpace
import com.wander.mono.core.apps.PrivateSpaceState
import com.wander.mono.core.tiles.TileRequest.Companion.openMono
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * The Private space tile: on while the space is unlocked; a tap brings Mono to the front to lock
 * or unlock it (unlocking shows the space's own lock). Unavailable before Android 15, and while
 * there is no private space Mono may see. While the shade shows it, the tile follows the profile
 * broadcasts [PrivateSpace] already listens to.
 */
@AndroidEntryPoint
class PrivateSpaceTileService : TileService() {
    @Inject
    lateinit var privateSpace: PrivateSpace

    private val scope: CoroutineScope = MainScope()
    private var listening: Job? = null
    private var shown: PrivateSpaceState = PrivateSpaceState.Absent

    override fun onStartListening() {
        super.onStartListening()
        listening?.cancel()
        listening = scope.launch { privateSpace.state.collect(::show) }
    }

    override fun onStopListening() {
        listening?.cancel()
        listening = null
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        val space = shown as? PrivateSpaceState.Present ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return
        openMono(if (space.locked) TileRequest.UnlockPrivateSpace else TileRequest.LockPrivateSpace)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun show(state: PrivateSpaceState) {
        shown = state
        val tile = qsTile ?: return
        if (state is PrivateSpaceState.Present && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val locked = state.locked
            tile.state = if (locked) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
            tile.icon = Icon.createWithResource(this, if (locked) R.drawable.sym_lock else R.drawable.sym_lock_open)
            tile.subtitle = getString(if (locked) R.string.private_tile_locked else R.string.private_tile_unlocked)
        } else {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.icon = Icon.createWithResource(this, R.drawable.sym_lock)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) tile.subtitle = null
        }
        tile.updateTile()
    }
}
