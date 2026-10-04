package com.example.game.ui

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * On-screen virtual touch controls:
 * - Floating/Fixed virtual joystick on bottom-left for smooth 360-degree walking
 * - SNES-styled Action Buttons A and B on bottom-right
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
        // Virtual Joystick (Bottom Left)
        val joystickRadiusPx = 140f
        var knobOffset by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 28.dp)
                .size(130.dp)
                .pointerInteropFilter { event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                            val centerX = 130f * 1.5f
                            val centerY = 130f * 1.5f
                            val dx = event.x - (130f / 2f * 2.5f)
                            val dy = event.y - (130f / 2f * 2.5f)
                            val dist = sqrt(dx * dx + dy * dy)
                            val maxDist = joystickRadiusPx

                            if (dist > 0f) {
                                val clampedDist = min(dist, maxDist)
                                val normX = dx / dist
                                val normY = dy / dist
                                knobOffset = Offset(normX * clampedDist, normY * clampedDist)
                                onJoystickMove(normX * (clampedDist / maxDist), normY * (clampedDist / maxDist))
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
            // Joystick Outer Ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                drawCircle(
                    color = Color(0x77101826),
                    radius = size.width / 2f,
                    center = center
                )
                drawCircle(
                    color = Color(0xAAFFD700),
                    radius = size.width / 2f,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                )
                // Directional D-pad notches
                drawCircle(
                    color = Color(0x44FFFFFF),
                    radius = size.width / 4f,
                    center = center
                )
            }

            // Joystick Knob (Thumb)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset { IntOffset(knobOffset.x.toInt(), knobOffset.y.toInt()) }
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xDD3B82F6))
                    .border(2.dp, Color(0xFF93C5FD), CircleShape)
            )
        }

        // SNES Action Buttons (Bottom Right: A and B)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Button B (Skill / Dash / Blue)
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(Color(0xEE1E40AF))
                    .border(3.dp, Color(0xFF60A5FA), CircleShape)
                    .clickable { onButtonBPressed() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "B",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Button A (Attack / Interact / Red)
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .offset(y = (-14).dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDC2626))
                    .border(3.dp, Color(0xFFFCA5A5), CircleShape)
                    .clickable { onButtonAPressed() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
