package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.game.graphics.PixelArtRenderer
import com.example.game.model.DecoType
import com.example.game.model.Direction
import com.example.game.viewmodel.GameViewModel
import com.example.game.world.CHUNK_SIZE
import kotlinx.coroutines.isActive

/**
 * 60 FPS Compose Canvas rendering the 2D procedural open world with SNES pixel art aesthetic.
 */
@Composable
fun OverworldCanvas(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    // Re-draw heartbeat driven by frame ticker
    var frameTick by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { frameTick = it.toFloat() }
        }
    }

    val player = viewModel.player
    val cameraX = viewModel.cameraX
    val cameraY = viewModel.cameraY
    val activeChunks = viewModel.worldManager.getActiveChunks()

    // Base tile display size on screen (e.g. 64px for chunky 2x pixel art feel on modern phones)
    val displayTileSize = 72f

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Read frameTick to trigger recomposition every frame
        val _tick = frameTick

        val screenCenterX = size.width / 2f
        val screenCenterY = size.height / 2f

        // Helper to convert world coordinates to screen pixel coordinates
        fun toScreenX(worldX: Float): Float = screenCenterX + (worldX - cameraX) * displayTileSize
        fun toScreenY(worldY: Float): Float = screenCenterY + (worldY - cameraY) * displayTileSize

        // 1. RENDER CHUNKS (Terrain Tiles)
        for (chunk in activeChunks) {
            val chunkBaseX = chunk.chunkX * CHUNK_SIZE
            val chunkBaseY = chunk.chunkY * CHUNK_SIZE

            for (x in 0 until CHUNK_SIZE) {
                val worldX = (chunkBaseX + x).toFloat()
                val screenX = toScreenX(worldX)

                // Culling: check if column is on screen
                if (screenX < -displayTileSize || screenX > size.width) continue

                for (y in 0 until CHUNK_SIZE) {
                    val worldY = (chunkBaseY + y).toFloat()
                    val screenY = toScreenY(worldY)

                    if (screenY < -displayTileSize || screenY > size.height) continue

                    val tileType = chunk.tiles[x][y]
                    val tileBitmap = PixelArtRenderer.getTileBitmap(tileType)

                    drawImage(
                        image = tileBitmap,
                        dstOffset = IntOffset(screenX.toInt(), screenY.toInt()),
                        dstSize = IntSize(displayTileSize.toInt(), displayTileSize.toInt()),
                        filterQuality = FilterQuality.None
                    )
                }
            }
        }

        // 2. RENDER DECORATIONS (Trees, Rocks, Pillars, Altar)
        for (chunk in activeChunks) {
            val chunkBaseX = chunk.chunkX * CHUNK_SIZE
            val chunkBaseY = chunk.chunkY * CHUNK_SIZE

            for (x in 0 until CHUNK_SIZE) {
                val worldX = (chunkBaseX + x).toFloat()
                val screenX = toScreenX(worldX)
                if (screenX < -displayTileSize || screenX > size.width) continue

                for (y in 0 until CHUNK_SIZE) {
                    val deco = chunk.decos[x][y]
                    if (deco == DecoType.NONE) continue

                    val worldY = (chunkBaseY + y).toFloat()
                    val screenY = toScreenY(worldY)
                    if (screenY < -displayTileSize || screenY > size.height) continue

                    val decoBitmap = PixelArtRenderer.getDecoBitmap(deco)
                    if (decoBitmap != null) {
                        drawImage(
                            image = decoBitmap,
                            dstOffset = IntOffset(screenX.toInt(), screenY.toInt()),
                            dstSize = IntSize(displayTileSize.toInt(), displayTileSize.toInt()),
                            filterQuality = FilterQuality.None
                        )
                    }
                }
            }
        }

        // 3. RENDER CHESTS
        for (chunk in activeChunks) {
            for (chest in chunk.chests) {
                val screenX = toScreenX(chest.worldX)
                val screenY = toScreenY(chest.worldY)
                if (screenX in -displayTileSize..size.width && screenY in -displayTileSize..size.height) {
                    val chestBmp = PixelArtRenderer.getChestBitmap(chest.isOpen)
                    drawImage(
                        image = chestBmp,
                        dstOffset = IntOffset(screenX.toInt(), screenY.toInt()),
                        dstSize = IntSize(displayTileSize.toInt(), displayTileSize.toInt()),
                        filterQuality = FilterQuality.None
                    )
                }
            }
        }

        // 4. RENDER PASSIVE ANIMALS
        for (chunk in activeChunks) {
            for (animal in chunk.animals) {
                if (!animal.isAlive) continue
                val screenX = toScreenX(animal.worldX)
                val screenY = toScreenY(animal.worldY)
                if (screenX in -displayTileSize..size.width && screenY in -displayTileSize..size.height) {
                    val animalBmp = PixelArtRenderer.getAnimalBitmap(animal.spriteKey)
                    drawImage(
                        image = animalBmp,
                        dstOffset = IntOffset(screenX.toInt(), screenY.toInt()),
                        dstSize = IntSize(displayTileSize.toInt(), displayTileSize.toInt()),
                        filterQuality = FilterQuality.None
                    )
                }
            }
        }

        // 5. RENDER NPCS
        for (chunk in activeChunks) {
            for (npc in chunk.npcs) {
                val screenX = toScreenX(npc.worldX)
                val screenY = toScreenY(npc.worldY)
                if (screenX in -displayTileSize..size.width && screenY in -displayTileSize..size.height) {
                    val npcBmp = PixelArtRenderer.getNpcBitmap(npc.spriteKey)
                    drawImage(
                        image = npcBmp,
                        dstOffset = IntOffset(screenX.toInt(), screenY.toInt()),
                        dstSize = IntSize(displayTileSize.toInt(), displayTileSize.toInt()),
                        filterQuality = FilterQuality.None
                    )
                    // Quest marker bubble above NPC
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = 8f,
                        center = Offset(screenX + displayTileSize / 2f, screenY - 8f)
                    )
                }
            }
        }

        // 6. RENDER ENEMIES
        for (chunk in activeChunks) {
            for (enemy in chunk.enemies) {
                if (!enemy.isAlive) continue
                val screenX = toScreenX(enemy.worldX)
                val screenY = toScreenY(enemy.worldY)
                if (screenX in -displayTileSize..size.width && screenY in -displayTileSize..size.height) {
                    val enemyBmp = PixelArtRenderer.getEnemyOverworldBitmap(enemy.spriteKey)
                    drawImage(
                        image = enemyBmp,
                        dstOffset = IntOffset(screenX.toInt(), screenY.toInt()),
                        dstSize = IntSize(displayTileSize.toInt(), displayTileSize.toInt()),
                        filterQuality = FilterQuality.None
                    )
                    // Aggro indicator
                    if (enemy.isAggroed) {
                        drawCircle(
                            color = Color(0xFFFF1744),
                            radius = 6f,
                            center = Offset(screenX + displayTileSize / 2f, screenY - 6f)
                        )
                    }
                }
            }
        }

        // 7. RENDER PLAYER (HERO)
        val playerScreenX = toScreenX(player.worldX)
        val playerScreenY = toScreenY(player.worldY)

        // Invulnerability flicker
        val shouldDrawPlayer = player.invulnerableTimer <= 0f || ((player.invulnerableTimer * 10).toInt() % 2 == 0)

        if (shouldDrawPlayer) {
            val walkFrame = if (player.isMoving) ((player.walkAnimTimer.toInt()) % 3) else 0
            val playerBmp = PixelArtRenderer.getPlayerBitmap(player.direction, walkFrame, player.isAttacking)

            drawImage(
                image = playerBmp,
                dstOffset = IntOffset(playerScreenX.toInt(), playerScreenY.toInt()),
                dstSize = IntSize(displayTileSize.toInt(), displayTileSize.toInt()),
                filterQuality = FilterQuality.None
            )

            // Attack slash aura
            if (player.isAttacking) {
                drawCircle(
                    color = Color(0x66FFD700),
                    radius = displayTileSize * 0.7f,
                    center = Offset(playerScreenX + displayTileSize / 2f, playerScreenY + displayTileSize / 2f)
                )
            }
        }
    }
}
