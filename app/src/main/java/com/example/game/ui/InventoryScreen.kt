package com.example.game.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.graphics.PixelArtRenderer
import com.example.game.model.CraftingRecipe
import com.example.game.model.Item
import com.example.game.model.ItemCatalog
import com.example.game.model.ItemType
import com.example.game.viewmodel.GameViewModel

/**
 * Fullscreen Inventory, Equipment and Crafting Menu with retro SNES aesthetics.
 */
@Composable
fun InventoryScreen(
    viewModel: GameViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Mochila, 1: Alquimia y Forja, 2: Atributos
    var selectedItem by remember { mutableStateOf<Item?>(viewModel.player.inventory.firstOrNull()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xF50B0F19))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // HEADER BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MENU DE PERSONAJE",
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Cerrar ✕", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TAB BAR (Mochila, Crafteo, Estadísticas)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1E293B),
                contentColor = Color(0xFFFFD700)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Mochila (4x5)", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Forja & Alquimia", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Estadísticas", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // TAB CONTENT
            when (selectedTab) {
                0 -> InventoryGridTab(
                    viewModel = viewModel,
                    selectedItem = selectedItem,
                    onSelectItem = { selectedItem = it }
                )
                1 -> CraftingTab(viewModel = viewModel)
                2 -> CharacterStatsTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun InventoryGridTab(
    viewModel: GameViewModel,
    selectedItem: Item?,
    onSelectItem: (Item) -> Unit
) {
    val player = viewModel.player
    val inventory = player.inventory

    Column(modifier = Modifier.fillMaxSize()) {
        // Equipment Display (Equipped Weapon & Armor)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E293B))
                .border(1.5.dp, Color(0xFFD97706), RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Arma Equipada", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(player.equippedWeapon.name, color = Color(0xFF60A5FA), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("+${player.equippedWeapon.attackBonus} Ataque", color = Color(0xFF10B981), fontSize = 10.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Armadura Equipada", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(player.equippedArmor.name, color = Color(0xFFF472B6), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("+${player.equippedArmor.defenseBonus} Defensa", color = Color(0xFF10B981), fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4x5 Grid (20 Slots)
        Text("Objetos en Mochila (${inventory.size}/20):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A))
                .border(2.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(20) { index ->
                val item = inventory.getOrNull(index)
                val isSelected = (item != null && item.id == selectedItem?.id)

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Color(0xFF374151) else Color(0xFF1E293B))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFFFFD700) else Color(0xFF475569),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable(enabled = item != null) { if (item != null) onSelectItem(item) },
                    contentAlignment = Alignment.Center
                ) {
                    if (item != null) {
                        val iconBmp = PixelArtRenderer.getItemIconBitmap(item.iconKey)
                        Image(bitmap = iconBmp, contentDescription = item.name, modifier = Modifier.size(34.dp))

                        // Stack count badge
                        if (item.stackCount > 1) {
                            Text(
                                text = "x${item.stackCount}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected Item Details & Action Buttons
        if (selectedItem != null) {
            val item = selectedItem
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .border(2.dp, Color(0xFFD97706), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.name, color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Valor: ${item.value} G", color = Color(0xFFFBBF24), fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(item.description, color = Color(0xFFCBD5E1), fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (item.type == ItemType.CONSUMABLE) {
                        Button(
                            onClick = { viewModel.useItem(item) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Usar", fontWeight = FontWeight.Bold)
                        }
                    } else if (item.type == ItemType.WEAPON || item.type == ItemType.ARMOR) {
                        Button(
                            onClick = { viewModel.useItem(item) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Equipar", fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { viewModel.discardItem(item) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Descartar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CraftingTab(viewModel: GameViewModel) {
    val recipes = ItemCatalog.RECIPES

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(recipes) { recipe ->
            CraftingRecipeCard(recipe = recipe, viewModel = viewModel)
        }
    }
}

@Composable
private fun CraftingRecipeCard(recipe: CraftingRecipe, viewModel: GameViewModel) {
    val player = viewModel.player

    // Check if player has all required ingredients
    val canCraft = recipe.ingredients.all { ing ->
        val itemInInv = player.inventory.find { it.id == ing.itemId }
        (itemInInv?.stackCount ?: 0) >= ing.quantity
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E293B))
            .border(1.5.dp, if (canCraft) Color(0xFF10B981) else Color(0xFF475569), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${recipe.resultItem.name} x${recipe.resultQuantity}",
                color = Color(0xFFFFD700),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { viewModel.craftRecipe(recipe) },
                enabled = canCraft,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF059669),
                    disabledContainerColor = Color(0xFF334155)
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Fabricar", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(recipe.description, color = Color(0xFF94A3B8), fontSize = 11.sp)

        Spacer(modifier = Modifier.height(8.dp))

        // Ingredients required
        Text("Materiales requeridos:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            recipe.ingredients.forEach { ing ->
                val inInv = player.inventory.find { it.id == ing.itemId }?.stackCount ?: 0
                val hasEnough = inInv >= ing.quantity
                Text(
                    text = "${ing.itemId} ($inInv/${ing.quantity})",
                    color = if (hasEnough) Color(0xFF34D399) else Color(0xFFF87171),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun CharacterStatsTab(viewModel: GameViewModel) {
    val player = viewModel.player

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E293B))
            .border(2.dp, Color(0xFFD97706), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("ESTADÍSTICAS DEL HÉROE", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Nivel del Jugador", color = Color.White)
            Text("${player.level}", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Experiencia (EXP)", color = Color.White)
            Text("${player.xp} / ${player.xpToNextLevel}", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Puntos de Salud Máximos", color = Color.White)
            Text("${player.maxHp} HP", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Puntos de Maná Máximos", color = Color.White)
            Text("${player.maxMp} MP", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Poder de Ataque Total", color = Color.White)
            Text("${player.totalAttack} (${player.baseStrength} base + ${player.equippedWeapon.attackBonus} arma)", color = Color(0xFFF97316), fontWeight = FontWeight.Bold)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Defensa Total", color = Color.White)
            Text("${player.totalDefense} (${player.baseDefense} base + ${player.equippedArmor.defenseBonus} armadura)", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Velocidad de Movimiento y Turno", color = Color.White)
            Text("${player.baseSpeed}", color = Color(0xFFA855F7), fontWeight = FontWeight.Bold)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Oro Acumulado", color = Color.White)
            Text("${player.gold} Monedas", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Orbe del Sol Ancestral", color = Color.White)
            Text(if (player.hasArtifact) "¡ENCONTRADO!" else "Pendiente de descubrir", color = if (player.hasArtifact) Color(0xFF10B981) else Color(0xFFEF4444), fontWeight = FontWeight.Bold)
        }
    }
}
