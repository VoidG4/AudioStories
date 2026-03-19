package com.example.unipiaudiostories.ui.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unipiaudiostories.ui.theme.Slate500
import com.example.unipiaudiostories.ui.theme.Slate800

/**
 * Reusable Card component for displaying a single statistic metric.
 * Includes an entrance animation for visual polish.
 *
 * @param icon The vector icon to display.
 * @param label The label text for the statistic (e.g., "Total Time").
 * @param value The value text to display (e.g., "120m").
 * @param color The theme color associated with this statistic.
 * @param delay The delay in milliseconds for the entrance animation.
 */
@Composable
fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    delay: Int
) {
    // Entrance animation state (simulating a fade-in effect)
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = delay),
        label = "fade"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .alpha(alpha)
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = color.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon Container with gradient background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(color.copy(alpha = 0.4f), color)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Data Text
            Column {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Slate500,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate800
                )
            }
        }
    }
}