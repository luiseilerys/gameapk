package com.example.game.ui

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Authentic Game Boy Touch Controls:
 * - Cross-shaped D-Pad on bottom-left with tactile response & 360-degree walking
 * - Angled A and B round red action buttons on bottom-right
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VirtualControls(
    onJoystickMove: (Float, Float) -> Unit,
    onButtonAPressed: () -> Unit,
    onButtonBPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // ==========================================
        // GAME BOY CROSS D-PAD (Bottom Left)
        // ==========================================
        var knobOffset by remember { mutableStateOf(Offset.Zero) }
        var boxSizePx by remember { mutableStateOf(Size(300f, 300f)) }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 24.dp)
                .size(136.dp)
                .onSizeChanged { boxSizePx = Size(it.width.toFloat(), it.height.toFloat()) }
                .pointerInteropFilter { event ->
                    val centerX = boxSizePx.width / 2f
                    val centerY = boxSizePx.height / 2f
                    val maxRadius = boxSizePx.width / 2f

                    when (event.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                            val dx = event.x - centerX
                            val dy = event.y - centerY
                            val dist = sqrt(dx * dx + dy * dy)

                            if (dist > 5f) {
                                val clampedDist = min(dist, maxRadius)
                                val normX = dx / dist
                                val normY = dy / dist
                                knobOffset = Offset(normX * (clampedDist * 0.5f), normY * (clampedDist * 0.5f))
                                onJoystickMove(normX * (clampedDist / maxRadius), normY * (clampedDist / maxRadius))
                            }
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            knobOffset = Offset.Zero
                            onJoystickMove(0f, 0f)
                            true
                        }
                        else -> false
                    }
                }
        ) {
            // Game Boy Cross D-Pad Graphic
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val armWidth = size.width * 0.34f
                val armLength = size.width * 0.94f

                // Outer base shadow
                drawCircle(
                    color = Color(0x660F172A),
                    radius = size.width / 2f,
                    center = Offset(cx, cy)
                )

                // D-Pad Cross Horizontal Bar
                drawRoundRect(
                    color = Color(0xFF1E2430),
                    topLeft = Offset(cx - armLength / 2f, cy - armWidth / 2f),
                    size = Size(armLength, armWidth),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
                // D-Pad Cross Vertical Bar
                drawRoundRect(
                    color = Color(0xFF1E2430),
                    topLeft = Offset(cx - armWidth / 2f, cy - armLength / 2f),
                    size = Size(armWidth, armLength),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Central indentation disc
                drawCircle(
                    color = Color(0xFF141922),
                    radius = armWidth * 0.42f,
                    center = Offset(cx, cy)
                )

                // Triangle Directional Grips
                // Up
                drawCircle(Color(0xFF333E50), radius = 3.dp.toPx(), center = Offset(cx, cy - armLength * 0.35f))
                // Down
                drawCircle(Color(0xFF333E50), radius = 3.dp.toPx(), center = Offset(cx, cy + armLength * 0.35f))
                // Left
                drawCircle(Color(0xFF333E50), radius = 3.dp.toPx(), center = Offset(cx - armLength * 0.35f, cy))
                // Right
                drawCircle(Color(0xFF333E50), radius = 3.dp.toPx(), center = Offset(cx + armLength * 0.35f, cy))
            }

            // Tactile feedback thumb nub
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset { IntOffset(knobOffset.x.toInt(), knobOffset.y.toInt()) }
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x993B82F6))
                    .border(2.dp, Color(0xFF93C5FD), CircleShape)
            )
        }

        // ==========================================
        // GAME BOY ANGLED A & B BUTTONS (Bottom Right)
        // ==========================================
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Button B (Positioned slightly lower)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF881337)) // Game Boy maroon / deep red
                        .border(3.dp, Color(0xFFE11D48), CircleShape)
                        .clickable { onButtonBPressed() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "B",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "B",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Button A (Positioned slightly higher)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (-16).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF9F1239))
                        .border(3.5.dp, Color(0xFFF43F5E), CircleShape)
                        .clickable { onButtonAPressed() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "A",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
