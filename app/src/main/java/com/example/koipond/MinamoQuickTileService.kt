package com.example.koipond

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/**
 * Android Quick Settings Tile (`minamo pond`):
 * Tapping the tile in the notification shade cycles between:
 *   1. Shuffling the pond biome & dropping a feast of pellets + algae wafer
 *   2. Summoning a Predator Ambush
 */
class MinamoQuickTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.let { tile ->
            tile.state = Tile.STATE_ACTIVE
            tile.label = "minamo [>°]"
            tile.contentDescription = "Tap to shuffle minamo pond or trigger predator ambush"
            tile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        val repo = WallpaperSettingsRepository(this)
        val actionSeq = repo.triggerQuickTileCommand()
        qsTile?.let { tile ->
            tile.state = Tile.STATE_ACTIVE
            tile.label = if (actionSeq % 2 == 0) "minamo [!hunt]" else "minamo [*feast]"
            tile.updateTile()
        }
    }
}
