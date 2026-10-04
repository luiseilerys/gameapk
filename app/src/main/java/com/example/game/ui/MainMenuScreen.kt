package com.example.game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.localization.GameLanguage
import com.example.game.localization.Strings
import com.example.game.viewmodel.GameViewModel

/**
 * Authentic Pokémon Game Boy Color Title Screen:
 * - Handheld bezel with battery LED indicator and retro casing
 * - 100% complete uncropped 4:3 pixel art title artwork (never cut off)
 * - Game Boy Pokémon typography and action buttons
 * - Language selection (Español / English) & Audio settings
 */
@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val hasSaveGame by viewModel.hasSaveGame.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()
    var showOptions by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(viewModel.audio.isSoundEnabled) }
    var musicEnabled by remember { mutableStateOf(viewModel.audio.isMusicEnabled) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1035), // Game Boy Color Atomic Purple tone
                        Color(0xFF0F172A),
                        Color(0xFF090D16)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ==========================================
            // GAME BOY COLOR RETRO SCREEN BEZEL
            // ==========================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                // Outer Bezel Chassis
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF222530))
                        .border(3.dp, Color(0xFF383D4D), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Top Bezel Header: Battery LED + Game Boy Color Branding
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Battery LED
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF1744))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "BATTERY",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            // Retro Branding
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "GAME BOY ",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "C",
                                    color = Color(0xFFFF3366),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "O",
                                    color = Color(0xFFFFCC00),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "L",
                                    color = Color(0xFF33CC66),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "O",
                                    color = Color(0xFF3399FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "R",
                                    color = Color(0xFFAA44FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // SCREEN DISPLAY: 100% UN-CROPPED 4:3 PIXEL ART TITLE ARTWORK
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF080B12))
                                .border(2.dp, Color(0xFF101420), RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_pokemon_title),
                                contentDescription = "Portada Pokémon Aethelgard",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Game Title & Edition Banner
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xF0102868)) // Pokémon Indigo
                        .border(2.5.dp, Color(0xFFFFCC00), RoundedCornerShape(8.dp)) // Pokémon Gold
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = Strings.getTitle(lang),
                        color = Color(0xFFFFCC00),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (lang == GameLanguage.SPANISH) "— EDICIÓN TITÁN —" else "— TITAN VERSION —",
                    color = Color(0xFFEF4444),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // ACTION BUTTONS OR OPTIONS SUBPANEL
            // ==========================================
            if (!showOptions) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // New Game Button
                    Button(
                        onClick = { viewModel.startNewGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "▶  " + Strings.getNewGame(lang),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    // Continue Game Button
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
                            .height(50.dp)
                    ) {
                        Text(
                            text = if (hasSaveGame) "▶  " + Strings.getContinue(lang) else Strings.getNoSave(lang),
                            color = if (hasSaveGame) Color.White else Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Options Button
                    Button(
                        onClick = { showOptions = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text(
                            text = "⚙  " + Strings.getAudioOptions(lang),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // OPTIONS & LANGUAGE SELECTOR SUBPANEL
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xF00F172A))
                        .border(2.dp, Color(0xFFFFCC00), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⚙ " + Strings.getAudioOptions(lang),
                        color = Color(0xFFFFD700),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Language Selector (Spanish & English)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = Strings.getLanguageSetting(lang),
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.setLanguage(GameLanguage.SPANISH) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (lang == GameLanguage.SPANISH) Color(0xFFD97706) else Color(0xFF334155)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "🇪🇸 Español",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = if (lang == GameLanguage.SPANISH) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            Button(
                                onClick = { viewModel.setLanguage(GameLanguage.ENGLISH) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (lang == GameLanguage.ENGLISH) Color(0xFFD97706) else Color(0xFF334155)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "🇬🇧 English",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = if (lang == GameLanguage.ENGLISH) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // SFX Sound Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(Strings.getSfxSetting(lang), color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = {
                                soundEnabled = it
                                viewModel.audio.isSoundEnabled = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD700))
                        )
                    }

                    // Music Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(Strings.getMusicSetting(lang), color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = musicEnabled,
                            onCheckedChange = {
                                musicEnabled = it
                                viewModel.audio.toggleMusic(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD700))
                        )
                    }

                    // Back Button
                    Button(
                        onClick = { showOptions = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = Strings.getBack(lang),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
