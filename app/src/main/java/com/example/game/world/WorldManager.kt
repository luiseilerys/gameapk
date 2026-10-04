package com.example.game.world

import com.example.game.model.Biome
import com.example.game.model.ChestEntity
import com.example.game.model.EnemyEntity
import com.example.game.model.Player
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages chunk streaming, in-memory chunk cache, active chunks around player,
 * and persistent state of opened chests / defeated enemies.
 */
class WorldManager(val generator: WorldGenerator = WorldGenerator()) {
    // In-memory cache for all visited chunks
    private val chunkCache = ConcurrentHashMap<Pair<Int, Int>, Chunk>()

    // Track state of modified entities
    val openedChestIds = ConcurrentHashMap.newKeySet<String>()
    val defeatedEnemyIds = ConcurrentHashMap.newKeySet<String>()

    // Currently active chunks loaded around player
    private val activeChunks = mutableListOf<Chunk>()

    val loadRadius = 2 // 5x5 chunks around player (radius 2 ensures seamless 32x32 borders)

    fun getActiveChunks(): List<Chunk> = synchronized(activeChunks) { activeChunks.toList() }

    fun getChunk(chunkX: Int, chunkY: Int): Chunk {
        return chunkCache.computeIfAbsent(chunkX to chunkY) {
            val chunk = generator.generateChunk(chunkX, chunkY)
            // Filter already opened chests or defeated enemies
            chunk.chests.forEach { if (openedChestIds.contains(it.id)) it.isOpen = true }
            chunk.enemies.removeAll { defeatedEnemyIds.contains(it.id) }
            chunk
        }
    }

    /**
     * Updates loaded chunks around the player's current position.
     */
    fun updatePlayerPosition(playerX: Float, playerY: Float) {
        val playerChunkX = Math.floorDiv(playerX.toInt(), CHUNK_SIZE)
        val playerChunkY = Math.floorDiv(playerY.toInt(), CHUNK_SIZE)

        val newActive = mutableListOf<Chunk>()
        for (dx in -loadRadius..loadRadius) {
            for (dy in -loadRadius..loadRadius) {
                val cx = playerChunkX + dx
                val cy = playerChunkY + dy
                val chunk = getChunk(cx, cy)
                chunk.isExplored = true
                newActive.add(chunk)
            }
        }

        synchronized(activeChunks) {
            activeChunks.clear()
            activeChunks.addAll(newActive)
        }
    }

    /**
     * Checks if a world tile is walkable (both ground tile and deco collision).
     */
    fun isWalkable(worldTileX: Float, worldTileY: Float): Boolean {
        val tileX = worldTileX.toInt()
        val tileY = worldTileY.toInt()
        val chunkX = Math.floorDiv(tileX, CHUNK_SIZE)
        val chunkY = Math.floorDiv(tileY, CHUNK_SIZE)
        val localX = ((tileX % CHUNK_SIZE) + CHUNK_SIZE) % CHUNK_SIZE
        val localY = ((tileY % CHUNK_SIZE) + CHUNK_SIZE) % CHUNK_SIZE

        val chunk = getChunk(chunkX, chunkY)
        return chunk.isTileWalkable(localX, localY)
    }

    fun getBiomeAtPlayer(playerX: Float, playerY: Float): Biome {
        return generator.getBiomeAt(playerX.toInt(), playerY.toInt())
    }

    fun recordChestOpened(chestId: String) {
        openedChestIds.add(chestId)
    }

    fun recordEnemyDefeated(enemyId: String) {
        defeatedEnemyIds.add(enemyId)
        synchronized(activeChunks) {
            for (chunk in activeChunks) {
                chunk.enemies.removeAll { it.id == enemyId }
            }
        }
    }

    fun getExploredChunksCount(): Int {
        return chunkCache.values.count { it.isExplored }
    }
}
