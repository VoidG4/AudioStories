package com.example.unipiaudiostories.ui.story

import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.unipiaudiostories.R
import com.example.unipiaudiostories.data.Story
import com.example.unipiaudiostories.ui.home.LanguageSelector
import com.example.unipiaudiostories.ui.theme.*
import com.example.unipiaudiostories.utils.DeviceIdManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.Locale

// --- Data Models ---
data class FavoriteItem(
    val storyId: String = "",
    val title: String = "",
    val author: String = "",
    val imageUrl: String = "",
    val addedAt: Long = 0
)

data class HistoryItem(
    val storyId: String = "",
    val storyTitle: String = "",
    val timestamp: Long = 0,
    val completed: Boolean = false
)

/**
 * Main Player Screen.
 * Handles audio playback (TTS or MP3), text highlighting, Favorites toggle, and History logging.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryPlayerScreen(
    storyId: String?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // UI States
    var isLoading by remember { mutableStateOf(true) }
    var story by remember { mutableStateOf<Story?>(null) }
    var isFavorite by remember { mutableStateOf(false) }

    // Audio Player States
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var totalDuration by remember { mutableIntStateOf(0) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    var hasLoggedStart by remember { mutableStateOf(false) }

    val textScrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val mediaPlayer = remember { MediaPlayer() }
    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    // 1. Fetch Data from Firestore
    LaunchedEffect(storyId) {
        if (storyId != null) {
            val db = FirebaseFirestore.getInstance()
            val deviceId = DeviceIdManager.getDeviceId(context)
            try {
                val doc = db.collection("stories").document(storyId).get().await()
                story = doc.toObject(Story::class.java)?.copy(id = doc.id)
                val favDoc = db.collection("users").document(deviceId).collection("favorites").document(storyId).get().await()
                isFavorite = favDoc.exists()
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading data", Toast.LENGTH_LONG).show()
            }
        }
        isLoading = false
    }

    // Helper: Log History to Firestore
    fun logHistoryEvent(completed: Boolean) {
        if (story == null) return
        val deviceId = DeviceIdManager.getDeviceId(context)
        val historyItem = HistoryItem(
            storyId = story!!.id,
            storyTitle = story!!.title,
            timestamp = System.currentTimeMillis(),
            completed = completed
        )
        FirebaseFirestore.getInstance().collection("users").document(deviceId).collection("history").add(historyItem)
    }

    // Helper: Toggle Favorite Status
    fun toggleFavorite() {
        if (story == null) return
        val db = FirebaseFirestore.getInstance()
        val deviceId = DeviceIdManager.getDeviceId(context)
        val favoritesRef = db.collection("users").document(deviceId).collection("favorites")
        if (isFavorite) {
            favoritesRef.document(story!!.id).delete().addOnSuccessListener { isFavorite = false }
        } else {
            val favItem = FavoriteItem(story!!.id, story!!.title, story!!.author, story!!.imageUrl, System.currentTimeMillis())
            favoritesRef.document(story!!.id).set(favItem).addOnSuccessListener { isFavorite = true }
        }
    }

    // 2. Initialize TTS and MediaPlayer
    DisposableEffect(Unit) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) Toast.makeText(context, "TTS Init Failed", Toast.LENGTH_SHORT).show()
        }
        tts = ttsInstance

        ttsInstance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { isBuffering = true }
            override fun onDone(utteranceId: String?) {
                isBuffering = false
                if (utteranceId == "synth_story") {
                    val file = File(context.cacheDir, "story_audio.wav")
                    try {
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(file.path)
                        mediaPlayer.prepare()
                        totalDuration = mediaPlayer.duration
                        mediaPlayer.start()
                        isPlaying = true
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            mediaPlayer.playbackParams = mediaPlayer.playbackParams.apply { speed = playbackSpeed }
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
            override fun onError(utteranceId: String?) { isBuffering = false }
        })

        mediaPlayer.setOnCompletionListener {
            isPlaying = false
            currentPosition = 0
            logHistoryEvent(completed = true)
            Toast.makeText(context, context.getString(R.string.story_completed), Toast.LENGTH_SHORT).show()
        }

        onDispose {
            try {
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
                mediaPlayer.release()
                ttsInstance.stop()
                ttsInstance.shutdown()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // 3. Highlight Text Sync Loop
    LaunchedEffect(isPlaying) {
        if (isPlaying && !hasLoggedStart && totalDuration > 0) {
            logHistoryEvent(completed = false)
            hasLoggedStart = true
        }

        while (isPlaying) {
            if (mediaPlayer.isPlaying) {
                currentPosition = mediaPlayer.currentPosition
                if (totalDuration > 0 && story?.text != null && textLayoutResult != null) {
                    val correctionFactor = 1.02f
                    val rawIndex = (story!!.text.length * (currentPosition.toFloat() / totalDuration) * correctionFactor).toInt()
                    val estimatedCharIndex = rawIndex.coerceIn(0, story!!.text.length - 1)
                    try {
                        val line = textLayoutResult!!.getLineForOffset(estimatedCharIndex)
                        val lineTop = textLayoutResult!!.getLineTop(line)
                        textScrollState.animateScrollTo(lineTop.toInt())
                    } catch (e: Exception) { }
                }
            }
            delay(50)
        }
    }

    // --- Control Handlers ---
    val togglePlayPause = {
        if (!isBuffering) {
            if (totalDuration > 0) {
                if (mediaPlayer.isPlaying) { mediaPlayer.pause(); isPlaying = false }
                else { mediaPlayer.start(); isPlaying = true }
            } else {
                tts?.language = Locale.ENGLISH
                val destFile = File(context.cacheDir, "story_audio.wav")
                tts?.synthesizeToFile(story?.text ?: "", null, destFile, "synth_story")
                isBuffering = true
            }
        }
    }

    val handleSeek = { pos: Float -> if (totalDuration > 0) { mediaPlayer.seekTo(pos.toInt()); currentPosition = pos.toInt() } }
    val handleRestart = { if(totalDuration > 0) { mediaPlayer.seekTo(0); currentPosition = 0; if(!mediaPlayer.isPlaying) { mediaPlayer.start(); isPlaying = true } } }
    val handleChangeSpeed = {
        playbackSpeed = if(playbackSpeed == 1f) 1.5f else if(playbackSpeed == 1.5f) 2f else if(playbackSpeed == 2f) 0.75f else 1f
        if(totalDuration > 0 && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try { mediaPlayer.playbackParams = mediaPlayer.playbackParams.apply { speed = playbackSpeed }; if(!isPlaying && mediaPlayer.isPlaying) mediaPlayer.start() } catch (e: Exception){}
        }
    }

    // Animation for "Now Playing" Indicator
    val pulseScale by rememberInfiniteTransition("pulse").animateFloat(1f, 1.2f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), "pulse")

    Scaffold(
        topBar = {
            Surface(color = Color.White.copy(0.7f), modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate600)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.back), color = Slate600)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { toggleFavorite() }) {
                            Icon(if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, "Fav", tint = if (isFavorite) Color.Red else Slate600, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        LanguageSelector()
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Orange50, Color.White, Pink50)))) {
            if (isLoading) Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Orange500) }
            else if (story != null) {
                LazyColumn(contentPadding = PaddingValues(top = paddingValues.calculateTopPadding() + 16.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)) {
                    // Header Image & Title
                    item {
                        Box(Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(32.dp))) {
                            AsyncImage(model = story!!.imageUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.8f)), startY = 100f)))
                            Column(Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                                AnimatedVisibility(isPlaying) {
                                    Surface(color = Orange500, shape = RoundedCornerShape(50), modifier = Modifier.padding(bottom = 12.dp)) {
                                        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.size(8.dp).scale(pulseScale).background(Color.White, CircleShape)); Spacer(Modifier.width(8.dp));
                                            Text(stringResource(R.string.now_playing), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(story!!.title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp))
                                Text(story!!.author, color = Color.White.copy(0.8f), fontSize = 12.sp)
                            }
                        }
                    }

                    // Audio Controls
                    item {
                        Spacer(Modifier.height(24.dp))
                        if (isBuffering) Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) { CircularProgressIndicator(color = Orange500); Text(stringResource(R.string.creating_audio), Modifier.padding(top = 48.dp), color = Slate500, fontSize = 12.sp) }
                        else AudioPlayerUI(isPlaying, currentPosition, totalDuration, playbackSpeed, togglePlayPause, handleSeek, handleRestart, handleChangeSpeed)
                    }

                    // Story Text & Highlighting
                    item {
                        Spacer(Modifier.height(24.dp))
                        Card(colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(24.dp), modifier = Modifier.heightIn(min = 300.dp, max = 500.dp)) {
                            Column(Modifier.padding(24.dp)) {
                                Text(stringResource(R.string.story_text), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate700); Spacer(Modifier.height(16.dp))
                                Box(Modifier.weight(1f).verticalScroll(textScrollState)) {
                                    val fullText = story?.text ?: ""; val highlightedText = buildAnnotatedString {
                                    if (totalDuration > 0 && currentPosition > 0) {
                                        val correctionFactor = 1.02f
                                        val rawIndex = (fullText.length * (currentPosition.toFloat() / totalDuration) * correctionFactor).toInt()
                                        val idx = rawIndex.coerceIn(0, fullText.length)
                                        var s = idx; while(s > 0 && s < fullText.length && fullText[s-1] != ' ' && fullText[s-1] != '\n') s--
                                        var e = idx; while(e < fullText.length && fullText[e] != ' ' && fullText[e] != '\n') e++
                                        append(fullText.take(s.coerceIn(0, fullText.length)))
                                        withStyle(SpanStyle(background = Orange200, fontWeight = FontWeight.Bold)) { append(fullText.substring(s.coerceIn(0, fullText.length), e.coerceIn(s, fullText.length))) }
                                        append(fullText.substring(e.coerceIn(0, fullText.length)))
                                    } else append(fullText)
                                }
                                    Text(highlightedText, fontSize = 18.sp, color = Slate600, lineHeight = 28.sp, onTextLayout = { textLayoutResult = it })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}