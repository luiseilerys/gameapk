package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.graphics.PixelArtRenderer
import com.example.game.model.ActiveDialogue
import com.example.game.model.GameNotification
import com.example.game.viewmodel.GameViewModel
import com.example.game.world.CHUNK_SIZE

/**
 * Overworld Heads-Up Display (HUD):
 * - Health, Mana, XP status gauges
 * - Dynamic Minimap with radar indicators
 * - Quick action buttons (Inventory, Map, Pause)
 * - SNES-style interactive dialogue popups
 * - Real-time floating notifications
 */
@Composable
fun OverworldHUD(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val player = viewModel.player
    val biome by viewModel.currentBiome.collectAsState()
    val dialogue by viewModel.activeDialogue.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        // TOP BAR: Player Stats (Top Left) & Minimap + Controls (Top Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Player Stats Panel (Retro SNES frame)
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xDD111827))
                    .border(2.dp, Color(0xFFD97706), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Name & Level & Gold
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFD97706))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${com.example.game.localization.Strings.getLevelShort(lang)} ${player.level}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${com.example.game.localization.Strings.getGold(lang)}: ${player.gold} G",
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // HP Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "HP ",
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { (player.currentHp.toFloat() / player.maxHp).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .width(100.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFEF4444),
                        trackColor = Color(0xFF4B1818)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${player.currentHp}/${player.maxHp}",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // MP Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "MP ",
                        color = Color(0xFF3B82F6),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { (player.currentMp.toFloat() / player.maxMp).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .width(100.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF3B82F6),
                        trackColor = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${player.currentMp}/${player.maxMp}",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // XP Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "XP ",
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { (player.xp.toFloat() / player.xpToNextLevel).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .width(100.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF10B981),
                        trackColor = Color(0xFF064E3B)
                    )
                }

                // Current Biome Indicator
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = com.example.game.localization.Strings.getBiomeName(biome, lang),
                    color = Color(biome.primaryColorHex),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Top Right: Minimap and Quick Buttons
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                // Circular Minimap
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0xDD0F172A))
                        .border(2.dp, Color(0xFFD97706), CircleShape)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val miniScale = 2.5f

                        // Draw explored active chunks entities
                        val active = viewModel.worldManager.getActiveChunks()
                        for (chunk in active) {
                            // Chests (Gold dots)
                            for (c in chunk.chests) {
                                if (!c.isOpen) {
                                    val mx = center.x + (c.worldX - player.worldX) * miniScale
                                    val my = center.y + (c.worldY - player.worldY) * miniScale
                                    if ((mx - center.x) * (mx - center.x) + (my - center.y) * (my - center.y) < (size.width / 2f - 4) * (size.width / 2f - 4)) {
                                        drawCircle(Color(0xFFFFD700), radius = 3f, center = Offset(mx, my))
                                    }
                                }
                            }
                            // Enemies (Red dots)
                            for (e in chunk.enemies) {
                                if (e.isAlive) {
                                    val mx = center.x + (e.worldX - player.worldX) * miniScale
                                    val my = center.y + (e.worldY - player.worldY) * miniScale
                                    if ((mx - center.x) * (mx - center.x) + (my - center.y) * (my - center.y) < (size.width / 2f - 4) * (size.width / 2f - 4)) {
                                        drawCircle(Color(0xFFEF4444), radius = 3.5f, center = Offset(mx, my))
                                    }
                                }
                            }
                            // NPCs (Cyan dots)
                            for (n in chunk.npcs) {
                                val mx = center.x + (n.worldX - player.worldX) * miniScale
                                val my = center.y + (n.worldY - player.worldY) * miniScale
                                if ((mx - center.x) * (mx - center.x) + (my - center.y) * (my - center.y) < (size.width / 2f - 4) * (size.width / 2f - 4)) {
                                    drawCircle(Color(0xFF00E5FF), radius = 3.5f, center = Offset(mx, my))
                                }
                            }
                        }

                        // Artifact Beacon Marker (Pulsing Sun)
                        val artX = (viewModel.worldManager.generator.artifactChunkX * CHUNK_SIZE + 16).toFloat()
                        val artY = (viewModel.worldManager.generator.artifactChunkY * CHUNK_SIZE + 16).toFloat()
                        val adx = artX - player.worldX
                        val ady = artY - player.worldY
                        val adist = kotlin.math.sqrt(adx * adx + ady * ady)
                        val dirX = adx / (if (adist > 0f) adist else 1f)
                        val dirY = ady / (if (adist > 0f) adist else 1f)
                        val beaconOffset = Offset(
                            center.x + dirX * (size.width / 2f - 8f),
                            center.y + dirY * (size.height / 2f - 8f)
                        )
                        drawCircle(Color(0xFFFFEA00), radius = 5f, center = beaconOffset)

                        // Player Marker in center (Green dot)
                        drawCircle(Color(0xFF10B981), radius = 4.5f, center = center)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Action Buttons (Backpack, Map, Pause)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = { viewModel.openInventory() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xDD1F2937))
                            .border(1.5.dp, Color(0xFFD97706), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backpack,
                            contentDescription = "Inventario",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.openWorldMap() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xDD1F2937))
                            .border(1.5.dp, Color(0xFFD97706), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Mapa",
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.togglePause() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xDD1F2937))
                            .border(1.5.dp, Color(0xFFD97706), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pausa",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Active Notifications Stack (Top Center)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            notifications.forEach { notif ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xEE1E293B))
                        .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(6.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = notif.message,
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // SNES Interactive Dialogue Box (Bottom Center)
        if (dialogue != null) {
            val dlg = dialogue!!
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 18.dp, end = 18.dp, bottom = 110.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xF00A0F1D))
                    .border(3.dp, Color(0xFFD97706), RoundedCornerShape(8.dp))
                    .clickable { viewModel.onActionButtonAPressed() }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    // NPC Portrait
                    val npcBmp = PixelArtRenderer.getNpcBitmap(dlg.spriteKey)
                    Image(
                        bitmap = npcBmp,
                        contentDescription = dlg.speakerName,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.5.dp, Color(0xFFD97706), RoundedCornerShape(6.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    val (speakerName, speakerTitle, speakerText) = if (lang == com.example.game.localization.GameLanguage.ENGLISH) {
                        when (dlg.spriteKey) {
                            "npc_wizard" -> Triple(
                                "Elder Alden",
                                "Guardian of the Order",
                                listOf(
                                    "Greetings, brave adventurer! The balance of Aethelgard is crumbling.",
                                    "In the Forgotten Ruins to the east (Sector 10, 8) lies the Ancient Sun Orb.",
                                    "Beware: the Abyssal Titan will awaken once unsealed. Forge mighty gear!"
                                ).getOrElse(dlg.currentLineIndex) { dlg.lines[dlg.currentLineIndex] }
                            )
                            else -> Triple(
                                "Scout Roland",
                                "Wandering Explorer",
                                listOf(
                                    "The weather here is unpredictable. Gather herbs and ores to brew potions and equipment.",
                                    "Use the B button to dash or cast arcane skills in battle.",
                                    "I've heard the ancient Titan lurks beyond the mists once the artifact is claimed."
                                ).getOrElse(dlg.currentLineIndex) { dlg.lines[dlg.currentLineIndex] }
                            )
                        }
                    } else {
                        Triple(dlg.speakerName, dlg.speakerTitle, dlg.lines[dlg.currentLineIndex])
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = speakerName,
                            color = Color(0xFFFFD700),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = speakerTitle,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = speakerText,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = com.example.game.localization.Strings.getPressAToContinue(lang),
                            color = Color(0xFFA7F3D0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}
