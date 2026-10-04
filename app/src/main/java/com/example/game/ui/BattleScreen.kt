package com.example.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.graphics.PixelArtRenderer
import com.example.game.localization.GameLanguage
import com.example.game.localization.Strings
import com.example.game.model.BattleActionType
import com.example.game.model.BattleState
import com.example.game.model.Biome
import com.example.game.model.CombatSkill
import com.example.game.model.Item
import com.example.game.model.ItemType
import com.example.game.viewmodel.GameViewModel

/**
 * Authentic Pokémon Game Boy / Game Boy Color Turn-Based Battle Screen:
 * - Upper Right: Enemy Pokémon on oval battle podium with Top-Left HP Box
 * - Lower Left: Pokémon Trainer Back-Sprite on battle podium with Bottom-Right HP Box
 * - Bottom: Iconic double-lined Game Boy dialogue box with 2x2 Battle Commands (Luchar, Bolsa, Técnica, Huir)
 * - Floating damage numbers, attack lunges, and victory popup
 */
@Composable
fun BattleScreen(
    state: BattleState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    var selectedSubmenu by remember { mutableStateOf<String?>(null) } // "SKILLS", "ITEMS", or null

    val hero = viewModel.player
    val playerCombatant = state.playerCombatant
    val primaryEnemy = state.enemyCombatants.firstOrNull { it.isAlive } ?: state.enemyCombatants.first()

    // Determine battle background drawable based on biome
    val bgRes = when (state.biome) {
        Biome.RUINS -> R.drawable.battle_bg_ruins
        else -> R.drawable.battle_bg_forest
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Biome Backdrop Artwork
        Image(
            painter = painterResource(id = bgRes),
            contentDescription = "Fondo de batalla",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark ambient overlay for crisp Game Boy contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x88050811),
                            Color(0x44050811),
                            Color(0xCC050811)
                        )
                    )
                )
        )

        // 2. MAIN BATTLE ARENA (Top 70% of screen)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 12.dp, end = 12.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ==========================================
            // TOP AREA: ENEMY HP BOX (LEFT) & ENEMY MONSTER (RIGHT)
            // ==========================================
            Column(modifier = Modifier.fillMaxWidth()) {
                // Boss Banner if final boss
                if (primaryEnemy.isBoss) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xEE881337))
                            .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "★ TITÁN DEL ABISMO (JEFE FINAL) ★",
                            color = Color(0xFFFFCC00),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // ENEMY POKÉMON HP BOX (Classic Game Boy Style on Top Left)
                    Box(
                        modifier = Modifier
                            .width(170.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8F9FA))
                            .border(2.5.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = primaryEnemy.name.uppercase(),
                                    color = Color(0xFF0F172A),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = ":L5",
                                    color = Color(0xFF475569),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // HP BAR with Game Boy HP Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFFFFCC00))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "HP",
                                        color = Color(0xFF0F172A),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))

                                val hpRatio = (primaryEnemy.currentHp.toFloat() / primaryEnemy.maxHp).coerceIn(0f, 1f)
                                val hpColor = when {
                                    hpRatio > 0.5f -> Color(0xFF22C55E) // Green
                                    hpRatio > 0.2f -> Color(0xFFEAB308) // Yellow
                                    else -> Color(0xFFEF4444) // Red
                                }

                                LinearProgressIndicator(
                                    progress = { hpRatio },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = hpColor,
                                    trackColor = Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }

                    // ENEMY MONSTER ON OVAL PODIUM (Top Right)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.offset {
                            if (state.isAttackAnimating && state.activeAttackerId == primaryEnemy.id) {
                                IntOffset(-20, 20)
                            } else IntOffset.Zero
                        }
                    ) {
                        // Monster 64x64 Sprite
                        val enemyBmp = PixelArtRenderer.getEnemyBattleBitmap(primaryEnemy.spriteKey)
                        Image(
                            bitmap = enemyBmp,
                            contentDescription = primaryEnemy.name,
                            modifier = Modifier.size(if (primaryEnemy.isBoss) 120.dp else 90.dp),
                            filterQuality = FilterQuality.None
                        )

                        // Oval grassy battle podium
                        Canvas(modifier = Modifier.size(width = 110.dp, height = 22.dp)) {
                            drawOval(
                                color = Color(0xAA2D5A27),
                                size = size,
                                topLeft = Offset.Zero
                            )
                            drawOval(
                                color = Color(0xFF1B3D17),
                                size = size,
                                topLeft = Offset.Zero,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }
            }

            // FLOATING DAMAGE NUMBERS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                state.floatingTexts.forEach { ft ->
                    Text(
                        text = ft.text,
                        color = Color(ft.colorHex),
                        fontSize = if (ft.isCritical) 24.sp else 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .offset { IntOffset(ft.startX.toInt(), (ft.startY + ft.offsetY).toInt()) }
                            .alpha(ft.alpha)
                    )
                }
            }

            // ==========================================
            // BOTTOM AREA: PLAYER TRAINER (LEFT) & PLAYER HP BOX (RIGHT)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // PLAYER TRAINER BACK-SPRITE ON BATTLE PODIUM (Bottom Left)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset {
                        if (state.isAttackAnimating && state.activeAttackerId == playerCombatant.id) {
                            IntOffset(20, -20)
                        } else IntOffset.Zero
                    }
                ) {
                    val isAttacking = state.isAttackAnimating && state.activeAttackerId == playerCombatant.id
                    val trainerBackBmp = PixelArtRenderer.getTrainerBackSprite(isThrowing = isAttacking)

                    Image(
                        bitmap = trainerBackBmp,
                        contentDescription = "Entrenador Red",
                        modifier = Modifier.size(100.dp),
                        filterQuality = FilterQuality.None
                    )

                    // Oval Player Battle Podium
                    Canvas(modifier = Modifier.size(width = 110.dp, height = 22.dp)) {
                        drawOval(
                            color = Color(0xAA475569),
                            size = size,
                            topLeft = Offset.Zero
                        )
                        drawOval(
                            color = Color(0xFF1E293B),
                            size = size,
                            topLeft = Offset.Zero,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                // PLAYER HP & STATUS BOX (Bottom Right)
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8F9FA))
                        .border(2.5.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RED",
                                color = Color(0xFF0F172A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = ":L" + hero.level,
                                color = Color(0xFF475569),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // HP BAR
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFFFFCC00))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "HP",
                                    color = Color(0xFF0F172A),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))

                            val playerHpRatio = (playerCombatant.currentHp.toFloat() / playerCombatant.maxHp).coerceIn(0f, 1f)
                            val playerHpColor = when {
                                playerHpRatio > 0.5f -> Color(0xFF22C55E)
                                playerHpRatio > 0.2f -> Color(0xFFEAB308)
                                else -> Color(0xFFEF4444)
                            }

                            LinearProgressIndicator(
                                progress = { playerHpRatio },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = playerHpColor,
                                trackColor = Color(0xFFCBD5E1)
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        // HP Text numbers
                        Text(
                            text = "${playerCombatant.currentHp}/ ${playerCombatant.maxHp}",
                            color = Color(0xFF1E293B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.align(Alignment.End)
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // EXP Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EXP",
                                color = Color(0xFF2563EB),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            LinearProgressIndicator(
                                progress = { (hero.xp.toFloat() / hero.xpToNextLevel).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color(0xFF2563EB),
                                trackColor = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // CLASSIC GAME BOY BATTLE COMMAND DIALOGUE BOX (BOTTOM)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8F9FA))
                    .border(3.dp, Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                if (selectedSubmenu == null) {
                    // MAIN COMMANDS: Left message & Right 2x2 action buttons
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Prompt Question
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = if (lang == GameLanguage.SPANISH) "¿Qué hará RED?" else "What will RED do?",
                                color = Color(0xFF0F172A),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 20.sp
                            )
                        }

                        // Right: 2x2 Action Buttons
                        Column(
                            modifier = Modifier
                                .width(180.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE2E8F0))
                                .border(1.5.dp, Color(0xFF64748B), RoundedCornerShape(6.dp))
                                .padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.executePlayerAction(BattleActionType.ATTACK) },
                                    enabled = state.isPlayerTurn && !state.isAttackAnimating,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Text(
                                        text = if (lang == GameLanguage.SPANISH) "LUCHAR" else "FIGHT",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Button(
                                    onClick = { selectedSubmenu = "ITEMS" },
                                    enabled = state.isPlayerTurn && !state.isAttackAnimating,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Text(
                                        text = if (lang == GameLanguage.SPANISH) "BOLSA" else "BAG",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { selectedSubmenu = "SKILLS" },
                                    enabled = state.isPlayerTurn && !state.isAttackAnimating,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Text(
                                        text = if (lang == GameLanguage.SPANISH) "POKÉ" else "PKMN",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Button(
                                    onClick = { viewModel.executePlayerAction(BattleActionType.FLEE) },
                                    enabled = state.isPlayerTurn && !state.isAttackAnimating,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Text(
                                        text = if (lang == GameLanguage.SPANISH) "HUIR" else "RUN",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                } else if (selectedSubmenu == "SKILLS") {
                    // SKILLS / TECHNIQUES SUBMENU
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == GameLanguage.SPANISH) "MOVIMIENTOS POKÉMON" else "POKÉMON MOVES",
                                color = Color(0xFF0F172A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "◀ " + Strings.getBack(lang),
                                color = Color(0xFFDC2626),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { selectedSubmenu = null }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            playerCombatant.skills.take(2).forEach { skill ->
                                val canAfford = playerCombatant.currentMp >= skill.mpCost
                                Button(
                                    onClick = {
                                        selectedSubmenu = null
                                        viewModel.executePlayerAction(BattleActionType.SKILL, skill = skill)
                                    },
                                    enabled = canAfford && state.isPlayerTurn,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2563EB),
                                        disabledContainerColor = Color(0xFF94A3B8)
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.weight(1f).height(44.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(skill.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("PP: ${skill.mpCost}", color = Color(0xFFFFD700), fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedSubmenu == "ITEMS") {
                    // ITEMS / BAG SUBMENU
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == GameLanguage.SPANISH) "BOLSA DE OBJETOS" else "BAG ITEMS",
                                color = Color(0xFF0F172A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "◀ " + Strings.getBack(lang),
                                color = Color(0xFFDC2626),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { selectedSubmenu = null }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        val consumables = hero.inventory.filter { it.type == ItemType.CONSUMABLE && it.stackCount > 0 }
                        if (consumables.isEmpty()) {
                            Text(
                                text = if (lang == GameLanguage.SPANISH) "La bolsa está vacía." else "Bag is empty.",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                consumables.take(2).forEach { item ->
                                    Button(
                                        onClick = {
                                            selectedSubmenu = null
                                            viewModel.executePlayerAction(BattleActionType.ITEM, item = item)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.weight(1f).height(44.dp)
                                    ) {
                                        Text("${item.name} x${item.stackCount}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // VICTORY POPUP OVERLAY
        AnimatedVisibility(
            visible = state.isVictory,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8F9FA))
                    .border(3.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (lang == GameLanguage.SPANISH) "¡VICTORIA!" else "VICTORY!",
                        color = Color(0xFF16A34A),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "EXP: +${state.xpEarned} XP",
                        color = Color(0xFF1E293B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "¥: +${state.goldEarned} ¥",
                        color = Color(0xFFD97706),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.closeVictoryScreen() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = Strings.getContinue(lang),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
