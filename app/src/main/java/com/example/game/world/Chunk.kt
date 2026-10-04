package com.example.game.world

import com.example.game.model.AnimalEntity
import com.example.game.model.Biome
import com.example.game.model.ChestEntity
import com.example.game.model.DecoType
import com.example.game.model.EnemyEntity
import com.example.game.model.NpcEntity
import com.example.game.model.TileType

const val CHUNK_SIZE = 32
const val TILE_SIZE_PX = 32

/**
 * A 32x32 tile chunk representing a sector of the infinite procedural world.
 */
data class Chunk(
    val chunkX: Int,
    val chunkY: Int,
    val primaryBiome: Biome,
    val tiles: Array<Array<TileType>>,
    val decos: Array<Array<DecoType>>,
    val enemies: MutableList<EnemyEntity> = mutableListOf(),
    val animals: MutableList<AnimalEntity> = mutableListOf(),
    val chests: MutableList<ChestEntity> = mutableListOf(),
    val npcs: MutableList<NpcEntity> = mutableListOf(),
    var isExplored: Boolean = false,
    val isDungeonArtifactChunk: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Chunk
        return chunkX == other.chunkX && chunkY == other.chunkY
    }

    override fun hashCode(): Int {
        var result = chunkX
        result = 31 * result + chunkY
        return result
    }

    fun isTileWalkable(localX: Int, localY: Int): Boolean {
        if (localX !in 0 until CHUNK_SIZE || localY !in 0 until CHUNK_SIZE) return false
        val tile = tiles[localX][localY]
        val deco = decos[localX][localY]
        return tile.isWalkable && !deco.blocksMovement
    }
}
