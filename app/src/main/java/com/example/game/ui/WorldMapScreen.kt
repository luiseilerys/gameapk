package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.Biome
import com.example.game.viewmodel.GameViewModel
import com.example.game.world.CHUNK_SIZE
import kotlin.math.sqrt

/**
 * Fullscreen Interactive World Map:
 * - Zoom in / Zoom out controls
 * - Discovered biomes terrain rendering
 * - Player marker, ruins sanctuary marker, and legend
 */
@Composable
fun WorldMapScreen(
    viewModel: GameViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    val player = viewModel.player
    val artifactX = (viewModel.worldManager.generator.artifactChunkX * CHUNK_SIZE + 16).toFloat()
    val artifactY = (viewModel.worldManager.generator.artifactChunkY * CHUNK_SIZE + 16).toFloat()

    val distToArtifact = sqrt((artifactX - player.worldX) * (artifactX - player.worldX) + (artifactY - player.worldY) * (artifactY - player.worldY)).toInt()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xF50B0F19))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = com.example.game.localization.Strings.getWorldMapTitle(lang),
                    color = Color(0xFFFFD700),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { zoomLevel = (zoomLevel + 0.25f).coerceAtMost(2.0f) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("+ Zoom")
                    }
                    Button(
                        onClick = { zoomLevel = (zoomLevel - 0.25f).coerceAtLeast(0.6f) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("- Zoom")
                    }
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(com.example.game.localization.Strings.getClose(lang))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Objective Radar Notice
            Text(
                text = com.example.game.localization.Strings.getArtifactRadar(lang, distToArtifact),
                color = Color(0xFFA7F3D0),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Map Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .border(2.dp, Color(0xFFD97706), RoundedCornerShape(8.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val mapScale = 6f * zoomLevel

                    // Render grid of nearby chunks
                    val startChunkX = -4
                    val endChunkX = 14
                    val startChunkY = -4
                    val endChunkY = 12

                    for (cx in startChunkX..endChunkX) {
                        for (cy in startChunkY..endChunkY) {
                            val chunkWorldX = (cx * CHUNK_SIZE).toFloat()
                            val chunkWorldY = (cy * CHUNK_SIZE).toFloat()

                            val screenX = center.x + (chunkWorldX - player.worldX) * mapScale / 10f
                            val screenY = center.y + (chunkWorldY - player.worldY) * mapScale / 10f
                            val chunkSizeOnScreen = (CHUNK_SIZE * mapScale / 10f)

                            val biome = viewModel.worldManager.generator.getBiomeAt(
                                cx * CHUNK_SIZE + 16,
                                cy * CHUNK_SIZE + 16
                            )

                            drawRect(
                                color = Color(biome.primaryColorHex).copy(alpha = 0.65f),
                                topLeft = Offset(screenX, screenY),
                                size = Size(chunkSizeOnScreen, chunkSizeOnScreen)
                            )
                        }
                    }

                    // Ruins Dungeon Objective Altar Marker
                    val artScreenX = center.x + (artifactX - player.worldX) * mapScale / 10f
                    val artScreenY = center.y + (artifactY - player.worldY) * mapScale / 10f
                    drawCircle(Color(0xFFFFEA00), radius = 10f, center = Offset(artScreenX, artScreenY))
                    drawCircle(Color(0xFFFF1744), radius = 5f, center = Offset(artScreenX, artScreenY))

                    // Player Marker in center
                    drawCircle(Color(0xFF10B981), radius = 8f, center = center)
                    drawCircle(Color.White, radius = 3f, center = center)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Map Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                LegendItem("Tú", Color(0xFF10B981))
                LegendItem("Bosque", Color(Biome.FOREST.primaryColorHex))
                LegendItem("Desierto", Color(Biome.DESERT.primaryColorHex))
                LegendItem("Montaña", Color(Biome.MOUNTAIN.primaryColorHex))
                LegendItem("Ruinas (Objetivo)", Color(0xFFFFEA00))
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .padding(end = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
                .padding(horizontal = 6.dp, vertical = 4.dp)
        )
        Text(label, color = Color.White, fontSize = 11.sp)
    }
}
