package com.example.game.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.game.viewmodel.GameViewModel

/**
 * Pause Menu Modal with Resume, Inventory, Map, Save and Exit to Main Menu options.
 */
@Composable
fun PauseMenuDialog(
    viewModel: GameViewModel,
    onResume: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenMap: () -> Unit,
    onExitToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    var soundEnabled by remember { mutableStateOf(viewModel.audio.isSoundEnabled) }
    var musicEnabled by remember { mutableStateOf(viewModel.audio.isMusicEnabled) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
                .border(3.dp, Color(0xFFD97706), RoundedCornerShape(12.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = com.example.game.localization.Strings.getPauseTitle(lang),
                color = Color(0xFFFFD700),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Continuar
            MenuButton(
                label = com.example.game.localization.Strings.getResume(lang),
                color = Color(0xFF059669),
                onClick = onResume
            )

            // Inventario
            MenuButton(
                label = com.example.game.localization.Strings.getInventory(lang),
                color = Color(0xFF2563EB),
                onClick = onOpenInventory
            )

            // Mapa
            MenuButton(
                label = com.example.game.localization.Strings.getWorldMap(lang),
                color = Color(0xFF4F46E5),
                onClick = onOpenMap
            )

            // Guardar Partida
            MenuButton(
                label = com.example.game.localization.Strings.getSaveGame(lang),
                color = Color(0xFFD97706),
                onClick = { viewModel.saveCurrentGame() }
            )

            // Language Selector inside Pause Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = com.example.game.localization.Strings.getLanguageSetting(lang),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (lang == com.example.game.localization.GameLanguage.SPANISH) Color(0xFFD97706) else Color(0xFF334155))
                            .clickable { viewModel.setLanguage(com.example.game.localization.GameLanguage.SPANISH) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("ES", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (lang == com.example.game.localization.GameLanguage.ENGLISH) Color(0xFFD97706) else Color(0xFF334155))
                            .clickable { viewModel.setLanguage(com.example.game.localization.GameLanguage.ENGLISH) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("EN", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Audio Switches
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(com.example.game.localization.Strings.getSfxSetting(lang), color = Color.White, fontSize = 12.sp)
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = {
                        soundEnabled = it
                        viewModel.audio.isSoundEnabled = it
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD700))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(com.example.game.localization.Strings.getMusicSetting(lang), color = Color.White, fontSize = 12.sp)
                Switch(
                    checked = musicEnabled,
                    onCheckedChange = {
                        musicEnabled = it
                        viewModel.audio.toggleMusic(it)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD700))
                )
            }

            // Salir al Menú
            MenuButton(
                label = com.example.game.localization.Strings.getExitToMenu(lang),
                color = Color(0xFFDC2626),
                onClick = onExitToMenu
            )
        }
    }
}

@Composable
private fun MenuButton(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
    ) {
        Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
