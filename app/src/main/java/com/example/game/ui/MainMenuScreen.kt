package com.example.game.ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.viewmodel.GameViewModel

/**
 * Retro SNES Pixel Art Main Menu with rich title art cover and sound options.
 */
@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val hasSaveGame by viewModel.hasSaveGame.collectAsState()
    var showOptions by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(viewModel.audio.isSoundEnabled) }
    var musicEnabled by remember { mutableStateOf(viewModel.audio.isMusicEnabled) }

    Box(modifier = modifier.fillMaxSize()) {
        // SNES Title Cover Artwork
        Image(
            painter = painterResource(id = R.drawable.snes_title_cover),
            contentDescription = "Portada de Aethelgard",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient Dark Tint for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x77050811),
                            Color(0x99050811),
                            Color(0xF0050811)
                        )
                    )
                )
        )

        // CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Title Header (SNES Logo)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xDD0F172A))
                        .border(3.dp, Color(0xFFFFD700), RoundedCornerShape(8.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "A E T H E L G A R D",
                        color = Color(0xFFFFD700),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "— El Despertar del Titán —",
                    color = Color(0xFFFDE68A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Aventura 2D de Exploración & Combate SNES",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }

            // Main Buttons or Options Panel
            if (!showOptions) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(bottom = 30.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // New Game
                    Button(
                        onClick = { viewModel.startNewGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "⚔ NUEVA PARTIDA",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Continue Game
                    Button(
                        onClick = { viewModel.continueGame() },
                        enabled = hasSaveGame,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            disabledContainerColor = Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = if (hasSaveGame) "▶ CONTINUAR AVENTURA" else "(SIN PARTIDA GUARDADA)",
                            color = if (hasSaveGame) Color.White else Color(0xFF64748B),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Options
                    Button(
                        onClick = { showOptions = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "⚙ OPCIONES DE AUDIO",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Options Subpanel
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xEE0F172A))
                        .border(2.dp, Color(0xFFD97706), RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "AJUSTES DE AUDIO SNES",
                        color = Color(0xFFFFD700),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sonidos SFX (8-Bit)", color = Color.White, fontSize = 12.sp)
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Música Chiptune SNES", color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = musicEnabled,
                            onCheckedChange = {
                                musicEnabled = it
                                viewModel.audio.toggleMusic(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD700))
                        )
                    }

                    Button(
                        onClick = { showOptions = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Volver", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
