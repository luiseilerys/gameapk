package com.example.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.graphics.PixelArtRenderer
import com.example.game.model.BattleActionType
import com.example.game.model.BattleState
import com.example.game.model.Biome
import com.example.game.model.CombatSkill
import com.example.game.model.Combatant
import com.example.game.model.Direction
import com.example.game.model.Item
import com.example.game.model.ItemType
import com.example.game.viewmodel.GameViewModel

/**
 * Fullscreen SNES Turn-Based Battle Screen with classic RPG layout:
 * - Dynamic biome backdrop (Forest, Ruins, etc.)
 * - Enemies top, Player bottom
 * - Turn order initiative tracker
 * - Floating animated damage numbers
 * - Action command menu: Atacar, Habilidad, Objeto, Huir
 * - Victory summary popup
 */
@Composable
fun BattleScreen(
    state: BattleState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    var selectedSubmenu by remember { mutableStateOf<String?>(null) } // "SKILLS", "ITEMS", or null

    val player = state.playerCombatant
    val primaryEnemy = state.enemyCombatants.firstOrNull { it.isAlive } ?: state.enemyCombatants.first()

    // Determine battle background drawable based on biome
    val bgRes = when (state.biome) {
        Biome.RUINS -> R.drawable.battle_bg_ruins
        else -> R.drawable.battle_bg_forest
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Biome Backdrop Artwork
        Image(
            painter = painterResource(id = bgRes),
            contentDescription = "Fondo de batalla",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark ambient overlay for SNES contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x990A0E17),
                            Color(0x550A0E17),
                            Color(0xCC050811)
                        )
                    )
                )
        )

        // TOP SECTION: Final Boss Bar / Turn Order & Enemy Formation
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 14.dp, end = 14.dp, bottom = 12.dp)
        ) {
            // Boss Dedicated Health Gauge
            if (primaryEnemy.isBoss) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xEE1E1B2E))
                        .border(2.dp, Color(0xFFFF1744), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "★ TITÁN DEL ABISMO (JEFE FINAL) ★",
                        color = Color(0xFFFF5252),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (primaryEnemy.currentHp.toFloat() / primaryEnemy.maxHp).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color(0xFFFF1744),
                        trackColor = Color(0xFF4A0E17)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Top Status Bar: Turn Order Tracker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xDD0F172A))
                    .border(1.5.dp, Color(0xFFD97706), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = com.example.game.localization.Strings.getRound(lang, state.roundNumber),
                    color = Color(0xFFFFD700),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                // Turn order badges
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.turnOrder.forEach { combatant ->
                        val isCurrent = (combatant.isPlayer && state.isPlayerTurn) || (!combatant.isPlayer && !state.isPlayerTurn)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isCurrent) Color(0xFFD97706) else Color(0xFF334155))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (combatant.isPlayer) com.example.game.localization.Strings.getHero(lang) else combatant.name.take(6),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ENEMY FORMATION (Top Middle)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset {
                        // Attack animation displacement towards player
                        if (state.isAttackAnimating && state.activeAttackerId == primaryEnemy.id) {
                            IntOffset(0, 30)
                        } else IntOffset.Zero
                    }
                ) {
                    // Enemy Health Bar & Name
                    if (!primaryEnemy.isBoss) {
                        Text(
                            text = primaryEnemy.name,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { (primaryEnemy.currentHp.toFloat() / primaryEnemy.maxHp).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFEF4444),
                                trackColor = Color(0xFF450A0A)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${primaryEnemy.currentHp}/${primaryEnemy.maxHp}",
                                color = Color(0xFFFCA5A5),
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Large 64x64 Enemy Sprite
                    val enemyBmp = PixelArtRenderer.getEnemyBattleBitmap(primaryEnemy.spriteKey)
                    Image(
                        bitmap = enemyBmp,
                        contentDescription = primaryEnemy.name,
                        modifier = Modifier.size(if (primaryEnemy.isBoss) 130.dp else 90.dp)
                    )
                }

                // Floating Damage Numbers
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

            // PLAYER FORMATION (Bottom Area)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Player Sprite
                val playerBmp = PixelArtRenderer.getPlayerBitmap(Direction.UP, walkFrame = 0, isAttacking = state.isAttackAnimating && state.activeAttackerId == player.id)
                Image(
                    bitmap = playerBmp,
                    contentDescription = player.name,
                    modifier = Modifier
                        .size(76.dp)
                        .offset {
                            if (state.isAttackAnimating && state.activeAttackerId == player.id) {
                                IntOffset(0, -30)
                            } else IntOffset.Zero
                        }
                )

                // Player Stats Card
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xDD0F172A))
                        .border(2.dp, Color(0xFFD97706), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = player.name,
                        color = Color(0xFFFFD700),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("HP: ", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        LinearProgressIndicator(
                            progress = { (player.currentHp.toFloat() / player.maxHp).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .width(90.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFFEF4444),
                            trackColor = Color(0xFF450A0A)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${player.currentHp}/${player.maxHp}", color = Color.White, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("MP: ", color = Color(0xFF3B82F6), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        LinearProgressIndicator(
                            progress = { (player.currentMp.toFloat() / player.maxMp).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .width(90.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF3B82F6),
                            trackColor = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${player.currentMp}/${player.maxMp}", color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            // COMBAT LOG BANNER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xE60A0E17))
                    .border(1.5.dp, Color(0xFF475569), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = state.battleLog.lastOrNull() ?: "Tu turno para actuar.",
                    color = Color(0xFFA7F3D0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ACTION CONTROLS / SUBMENUS
            if (selectedSubmenu == "SKILLS") {
                // Skills Submenu
                SkillsSubmenu(
                    skills = player.skills,
                    currentMp = player.currentMp,
                    onSelectSkill = { skill ->
                        selectedSubmenu = null
                        viewModel.executePlayerAction(BattleActionType.SKILL, skill = skill)
                    },
                    onBack = { selectedSubmenu = null }
                )
            } else if (selectedSubmenu == "ITEMS") {
                // Items Submenu
                ItemsSubmenu(
                    items = viewModel.player.inventory.filter { it.type == ItemType.CONSUMABLE },
                    onSelectItem = { item ->
                        selectedSubmenu = null
                        viewModel.executePlayerAction(BattleActionType.ITEM, item = item)
                    },
                    onBack = { selectedSubmenu = null }
                )
            } else {
                // Primary Action Command Buttons (SNES RPG Bar)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BattleActionButton(
                        label = com.example.game.localization.Strings.getAttack(lang),
                        color = Color(0xFFDC2626),
                        enabled = state.isPlayerTurn && !state.isAttackAnimating,
                        onClick = { viewModel.executePlayerAction(BattleActionType.ATTACK) },
                        modifier = Modifier.weight(1f)
                    )

                    BattleActionButton(
                        label = com.example.game.localization.Strings.getSkill(lang),
                        color = Color(0xFF2563EB),
                        enabled = state.isPlayerTurn && !state.isAttackAnimating,
                        onClick = { selectedSubmenu = "SKILLS" },
                        modifier = Modifier.weight(1f)
                    )

                    BattleActionButton(
                        label = com.example.game.localization.Strings.getItem(lang),
                        color = Color(0xFF059669),
                        enabled = state.isPlayerTurn && !state.isAttackAnimating,
                        onClick = { selectedSubmenu = "ITEMS" },
                        modifier = Modifier.weight(1f)
                    )

                    BattleActionButton(
                        label = com.example.game.localization.Strings.getFlee(lang),
                        color = Color(0xFFD97706),
                        enabled = state.isPlayerTurn && !state.isAttackAnimating && !primaryEnemy.isBoss,
                        onClick = { viewModel.executePlayerAction(BattleActionType.FLEE) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // VICTORY SUMMARY MODAL
        if (state.isVictory && !primaryEnemy.isBoss) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000))
                    .clickable {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(3.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = com.example.game.localization.Strings.getVictory(lang),
                        color = Color(0xFFFFD700),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(com.example.game.localization.Strings.getRewards(lang), color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• +${state.xpEarned} EXP", color = Color(0xFF10B981), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("• +${state.goldEarned} Gold", color = Color(0xFFFFD700), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.closeVictoryScreen() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(com.example.game.localization.Strings.getContinueAdventure(lang), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BattleActionButton(
    label: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) color else Color(0xFF334155))
            .border(2.dp, if (enabled) Color.White.copy(alpha = 0.6f) else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (enabled) Color.White else Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SkillsSubmenu(
    skills: List<CombatSkill>,
    currentMp: Int,
    onSelectSkill: (CombatSkill) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xF00F172A))
            .border(2.dp, Color(0xFF2563EB), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Habilidades Especiales", color = Color(0xFF60A5FA), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Volver ✕", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onBack() })
        }

        Spacer(modifier = Modifier.height(6.dp))

        skills.forEach { skill ->
            val canAfford = currentMp >= skill.mpCost
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (canAfford) Color(0xFF1E293B) else Color(0xFF0F172A))
                    .clickable(enabled = canAfford) { onSelectSkill(skill) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(skill.name, color = if (canAfford) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(skill.description, color = Color(0xFF94A3B8), fontSize = 10.sp)
                }
                Text("${skill.mpCost} MP", color = if (canAfford) Color(0xFF38BDF8) else Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ItemsSubmenu(
    items: List<Item>,
    onSelectItem: (Item) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xF00F172A))
            .border(2.dp, Color(0xFF059669), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Objetos Consumibles", color = Color(0xFF34D399), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Volver ✕", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onBack() })
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (items.isEmpty()) {
            Text("No tienes consumibles en tu mochila.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
        } else {
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .clickable { onSelectItem(item) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(item.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(item.description, color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                    Text("x${item.stackCount}", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
