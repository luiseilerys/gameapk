package com.example.game.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.viewmodel.GameViewModel

/**
 * Victory Screen presented when the player retrieves the artifact and slays the Final Boss!
 */
@Composable
fun VictoryScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val durationSeconds = ((System.currentTimeMillis() - viewModel.gameStartTime) / 1000).toInt()
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1B4B),
                        Color(0xFF0F172A),
                        Color(0xFF042F2E)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xE60F172A))
                .border(3.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                .padding(24.dp)
        ) {
            Text(
                text = com.example.game.localization.Strings.getSupremeVictoryTitle(lang),
                color = Color(0xFFFFD700),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Text(
                text = com.example.game.localization.Strings.getSupremeVictoryDesc(lang),
                color = Color(0xFFE2E8F0),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Game Statistics
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatRow(com.example.game.localization.Strings.getPlayTime(lang), timeFormatted, Color(0xFF67E8F9))
                StatRow(com.example.game.localization.Strings.getLevelReached(lang), "${com.example.game.localization.Strings.getLevelShort(lang)} ${viewModel.player.level}", Color(0xFFFDE047))
                StatRow(com.example.game.localization.Strings.getEnemiesDefeated(lang), "${viewModel.enemiesDefeatedCount}", Color(0xFFF87171))
                StatRow(com.example.game.localization.Strings.getChestsOpened(lang), "${viewModel.chestsOpenedCount}", Color(0xFFFBBF24))
                StatRow(com.example.game.localization.Strings.getSectorsExplored(lang), "${viewModel.worldManager.getExploredChunksCount()} chunks", Color(0xFF4ADE80))
                StatRow("${com.example.game.localization.Strings.getGold(lang)}:", "${viewModel.player.gold} G", Color(0xFFFFD700))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.returnToMainMenu() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(com.example.game.localization.Strings.getExitToMenu(lang), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFF94A3B8), fontSize = 12.sp)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
