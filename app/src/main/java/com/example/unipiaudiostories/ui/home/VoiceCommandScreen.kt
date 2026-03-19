package com.example.unipiaudiostories.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unipiaudiostories.data.Story
import com.example.unipiaudiostories.ui.theme.*
import com.example.unipiaudiostories.utils.VoiceAssistant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Screen handling voice interactions for navigating or selecting stories.
 * Note: Uses hardcoded English for voice prompts to match the speech recognition engine.
 */
@Composable
fun VoiceCommandScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToStory: (String) -> Unit
) {
    val context = LocalContext.current

    // Status text (hardcoded English for consistency with voice engine)
    var statusText by remember { mutableStateOf("Initializing...") }
    var isListening by remember { mutableStateOf(false) }

    val isSpeaking = statusText.contains("Speaking")

    var stories by remember { mutableStateOf<List<Story>>(emptyList()) }

    // Fetch stories to allow selection by voice
    LaunchedEffect(Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val result = db.collection("stories").get().await()
            stories = result.documents.mapNotNull { it.toObject(Story::class.java)?.copy(id = it.id) }
        } catch (e: Exception) { e.printStackTrace() }
    }

    val voiceAssistant = remember {
        VoiceAssistant(
            context = context,
            onStatusChange = { msg, listening ->
                statusText = msg
                isListening = listening
            },
            onNavigateToStats = onNavigateToStats,
            onNavigateToStory = onNavigateToStory
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted && stories.isNotEmpty()) voiceAssistant.startSession(stories)
    }

    LaunchedEffect(stories) {
        if (stories.isNotEmpty()) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) { onDispose { voiceAssistant.shutdown() } }

    // --- Animations ---
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    // Pulse animation for "Listening" state
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = "pulseAlpha"
    )

    // Breathe animation for "Speaking" state
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "breatheScale"
    )

    // Icon Color Transitions
    val targetIconColor = when {
        isListening -> Orange500
        isSpeaking -> Color(0xFFBB86FC)
        else -> Slate500
    }
    val animatedIconColor by animateColorAsState(targetValue = targetIconColor, label = "iconColor", animationSpec = tween(500))


    // --- UI Layout ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(
                colors = listOf(Color(0xFF3E2D5C), Color(0xFF1A1A2E)),
                center = androidx.compose.ui.geometry.Offset(500f, 500f),
                radius = 1500f
            ))
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.align(Alignment.TopEnd).padding(32.dp).statusBarsPadding()
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(32.dp))
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Central Microphone Animation
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
                if (isListening) {
                    Box(modifier = Modifier.size(280.dp).scale(pulseScale).background(Orange500.copy(alpha = pulseAlpha * 0.5f), CircleShape))
                    Box(modifier = Modifier.size(230.dp).scale(pulseScale * 0.9f).background(Pink500.copy(alpha = pulseAlpha * 0.7f), CircleShape))
                    Box(modifier = Modifier.size(180.dp).scale(pulseScale * 0.8f).background(Orange400.copy(alpha = pulseAlpha), CircleShape))
                }

                Surface(
                    color = animatedIconColor,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer {
                            if (isSpeaking) {
                                scaleX = breatheScale
                                scaleY = breatheScale
                            }
                        }
                        .shadow(
                            elevation = if (isListening || isSpeaking) 30.dp else 10.dp,
                            shape = CircleShape,
                            spotColor = animatedIconColor
                        )
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(32.dp).fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(56.dp))

            Text(
                text = statusText,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.3f),
                        blurRadius = 10f
                    )
                )
            )

            AnimatedVisibility(visible = isListening) {
                Text(
                    text = "Listening...",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Show Retry button if not listening/speaking and error occurred
            AnimatedVisibility(visible = !isListening && !isSpeaking && statusText.contains("Retry")) {
                Button(
                    onClick = { voiceAssistant.retry() },
                    colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text("Tap to Retry", fontSize = 18.sp)
                }
            }
        }
    }
}