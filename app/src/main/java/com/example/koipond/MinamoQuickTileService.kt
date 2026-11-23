package com.example.koipond

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/**
 * Android Quick Settings Tile (`minamo`):
 * Tapping the tile in the notification shade cycles through:
 *   1. Shuffling the biome & dropping a feast wafer (`minamo [*feast]`)
 *   2. Summoning a majestic Ocean Giant — whale, dolphin pod, orca, or manta (`minamo [~giant]`)
 *   3. Triggering a Predator Ambush (`minamo [!hunt]`)
 */
class MinamoQuickTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.let { tile ->
            tile.state = Tile.STATE_ACTIVE
            tile.label = "minamo [>°]"
            tile.contentDescription = "Tap to feast, summon an ocean giant, or trigger predator ambush"
            tile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        val repo = WallpaperSettingsRepository(this)
        val actionSeq = repo.triggerQuickTileCommand()
        qsTile?.let { tile ->
            tile.state = Tile.STATE_ACTIVE
            tile.label = when (actionSeq % 3) {
                1 -> "minamo [*feast]"
                2 -> "minamo [~giant]"
                else -> "minamo [!hunt]"
            }
            tile.updateTile()
        }
    }
}
