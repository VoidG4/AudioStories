package com.example.unipiaudiostories.ui.story

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unipiaudiostories.ui.theme.*

/**
 * Reusable UI component for the Audio Player controls.
 * Includes Slider, Play/Pause, Restart, and Speed controls.
 */
@Composable
fun AudioPlayerUI(
    isPlaying: Boolean,
    currentPosition: Int,
    totalDuration: Int,
    playbackSpeed: Float,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onRestart: () -> Unit,
    onChangeSpeed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Orange200)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Seek Slider
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { onSeek(it) },
                valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(thumbColor = Orange500, activeTrackColor = Orange500, inactiveTrackColor = Orange100)
            )

            // Timestamps
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(currentPosition), fontSize = 12.sp, color = Slate400)
                Text(formatTime(totalDuration), fontSize = 12.sp, color = Slate400)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Control Buttons
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                IconButton(onClick = onRestart, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Slate600)
                }

                Button(
                    onClick = onPlayPause,
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Orange400, Pink500))), contentAlignment = Alignment.Center) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }

                TextButton(onClick = onChangeSpeed) {
                    Text(text = "${playbackSpeed}x", fontWeight = FontWeight.Bold, color = Slate600, fontSize = 16.sp)
                }
            }
        }
    }
}

/**
 * Formats milliseconds into a MM:SS string.
 */
fun formatTime(milliseconds: Int): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}