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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.game.graphics.PixelArtRenderer
import com.example.game.model.DecoType
import com.example.game.viewmodel.GameViewModel
import com.example.game.world.CHUNK_SIZE
import kotlinx.coroutines.isActive

/**
 * 60 FPS Compose Canvas rendering the 2D procedural Pokémon Game Boy world.
 * The camera is strictly and perpetually centered on the Pokémon Trainer character whenever they move.
 */
@Composable
fun OverworldCanvas(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    // Re-draw heartbeat driven by high-precision frame ticker
    var frameTick by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { frameTick = it.toFloat() }
        }
    }

    // Base tile display size on screen (72px chunky retro pixel grid)
    val displayTileSize = 72f

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C101A))
    ) {
        // Read frameTick to trigger 60 FPS continuous redraw
        val _tick = frameTick

        // Read player coordinates on every frame
        val player = viewModel.player
        val currentX = player.worldX
        val currentY = player.worldY

        // Always query the dynamically active loaded chunks on each frame
        val activeChunks = viewModel.worldManager.getActiveChunks()

        // Center calculation: Centers the character directly in the visible play area (above bottom controls)
        val screenCenterX = size.width / 2f
        val screenCenterY = (size.height - 100.dp.toPx()) / 2f

        // Helper to convert world coordinates to screen pixel coordinates
        // The character at (currentX, currentY) is locked strictly dead-center on the screen
        fun toScreenX(worldX: Float): Float = screenCenterX - (displayTileSize / 2f) + (worldX - currentX) * displayTileSize
        fun toScreenY(worldY: Float): Float = screenCenterY - (displayTileSize / 2f) + (worldY - currentY) * displayTileSize

        // 1. RENDER CHUNKS (Terrain Tiles)
        for (chunk in activeChunks) {
            val chunkBaseX = chunk.chunkX * CHUNK_SIZE
            val chunkBaseY = chunk.chunkY * CHUNK_SIZE

            for (x in 0 until CHUNK_SIZE) {
                val worldX = (chunkBaseX + x).toFloat()
                val screenX = toScreenX(worldX)

                // Culling: check if column is visible on screen
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

        // 2. RENDER DECORATIONS (Trees, Flowers, Boulders, Altars)
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

        // 3. RENDER POKÉBALL ITEMS / CHESTS
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

        // 4. RENDER PASSIVE POKÉMON / ANIMALS
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

        // 5. RENDER NPCS (Pokémon Professors / Gym Leaders)
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
                    // Quest indicator exclamation mark above NPC head
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = 8f,
                        center = Offset(screenX + displayTileSize / 2f, screenY - 8f)
                    )
                }
            }
        }

        // 6. RENDER WILD POKÉMON / OVERWORLD MONSTERS
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
                    // Aggro indicator (! above head like wild Pokémon encounter)
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

        // 7. RENDER PLAYER (POKÉMON TRAINER) - 100% LOCKED IN CENTER OF SCREEN
        val playerScreenX = screenCenterX - (displayTileSize / 2f)
        val playerScreenY = screenCenterY - (displayTileSize / 2f)

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
