package com.example.unipiaudiostories.ui.statistics

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.unipiaudiostories.R
import com.example.unipiaudiostories.ui.theme.*
import com.example.unipiaudiostories.ui.story.FavoriteItem
import com.example.unipiaudiostories.ui.story.HistoryItem
import com.example.unipiaudiostories.utils.DeviceIdManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class encapsulating all statistics metrics for the user.
 */
data class StatsData(
    val listenedCount: Int,
    val completedCount: Int,
    val totalMinutes: Int,
    val favoritesList: List<FavoriteItem>,
    val recentHistory: List<HistoryItem>
)

/**
 * Statistics Screen displaying user progress, history, and favorite stories.
 * Fetches data from Firestore based on the device ID.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var stats by remember { mutableStateOf<StatsData?>(null) }

    // Toggle between showing Favorites or Recent History
    var showFavoritesList by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val deviceId = DeviceIdManager.getDeviceId(context)
        val db = FirebaseFirestore.getInstance()

        try {
            val historySnapshot = db.collection("users").document(deviceId).collection("history")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            val history = historySnapshot.toObjects(HistoryItem::class.java)

            val favoritesSnapshot = db.collection("users").document(deviceId).collection("favorites").get().await()
            val favorites = favoritesSnapshot.toObjects(FavoriteItem::class.java)

            val listenedCount = history.count { !it.completed }
            val completedCount = history.count { it.completed }

            stats = StatsData(
                listenedCount = listenedCount,
                completedCount = completedCount,
                totalMinutes = completedCount * 10, // Assuming avg 10 mins per story for estimation
                favoritesList = favorites,
                recentHistory = history
            )
        } catch (e: Exception) { e.printStackTrace() }
        isLoading = false
    }

    Scaffold(
        topBar = {
            Surface(color = Color.White.copy(0.9f), modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate600)
                        Text(stringResource(R.string.back), color = Slate600)
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF3E8FF), Color.White, Orange50)))) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Orange500) }
            } else if (stats != null) {
                LazyColumn(contentPadding = PaddingValues(top = paddingValues.calculateTopPadding() + 16.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)) {
                    // Header
                    item {
                        Column(Modifier.fillMaxWidth().padding(bottom = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.my_statistics), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Text(stringResource(R.string.stats_subtitle), fontSize = 16.sp, color = Slate600)
                        }
                    }

                    // Stat Metric Cards
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Box(Modifier.weight(1f).clickable { showFavoritesList = false }) {
                                    StatCard(Icons.Default.Headphones, stringResource(R.string.stat_started), stats!!.listenedCount.toString(), Orange500, 0)
                                }
                                Box(Modifier.weight(1f)) {
                                    StatCard(Icons.Default.AccessTime, stringResource(R.string.stat_time), "${stats!!.totalMinutes}m", Purple500, 100)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Box(Modifier.weight(1f)) {
                                    StatCard(Icons.Default.TrendingUp, stringResource(R.string.stat_completed), stats!!.completedCount.toString(), Green500, 200)
                                }
                                Box(Modifier.weight(1f).clickable { showFavoritesList = true }) {
                                    StatCard(Icons.Default.Favorite, stringResource(R.string.stat_favorites), stats!!.favoritesList.size.toString(), Pink500, 300)
                                }
                            }
                        }
                    }

                    // List Section (Favorites vs History)
                    item {
                        Spacer(Modifier.height(32.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (showFavoritesList) Icons.Default.Favorite else Icons.Default.History, null, tint = if (showFavoritesList) Pink500 else Orange500, modifier = Modifier.padding(end = 8.dp))
                            Text(
                                if (showFavoritesList) stringResource(R.string.my_favorites) else stringResource(R.string.recent_activity),
                                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate800
                            )
                        }
                        Spacer(Modifier.height(16.dp))

                        Card(colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                if (showFavoritesList) {
                                    if (stats!!.favoritesList.isEmpty()) {
                                        Text(stringResource(R.string.no_favorites), color = Slate500, modifier = Modifier.padding(16.dp))
                                    } else {
                                        stats!!.favoritesList.forEachIndexed { idx, item ->
                                            Row(Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                AsyncImage(model = item.imageUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)))
                                                Spacer(Modifier.width(12.dp))
                                                Column { Text(item.title, fontWeight = FontWeight.Bold); Text(item.author, fontSize = 12.sp, color = Slate400) }
                                                Spacer(Modifier.weight(1f)); Icon(Icons.Default.Favorite, null, tint = Pink500)
                                            }
                                            if (idx < stats!!.favoritesList.size - 1) HorizontalDivider(color = Slate100)
                                        }
                                    }
                                } else {
                                    if (stats!!.recentHistory.isEmpty()) {
                                        Text(stringResource(R.string.start_listening_hint), color = Slate500, modifier = Modifier.padding(16.dp))
                                    } else {
                                        stats!!.recentHistory.forEachIndexed { idx, item ->
                                            Row(Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(if(item.completed) Icons.Default.CheckCircle else Icons.Default.PlayCircle, null, tint = if(item.completed) Green500 else Orange500, modifier = Modifier.size(32.dp))
                                                Spacer(Modifier.width(12.dp))
                                                Column {
                                                    Text(item.storyTitle, fontWeight = FontWeight.Medium)
                                                    val date = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(item.timestamp))
                                                    val status = if(item.completed) stringResource(R.string.stat_completed) else stringResource(R.string.stat_started)
                                                    Text("${date} • $status", fontSize = 12.sp, color = Slate400)
                                                }
                                            }
                                            if (idx < stats!!.recentHistory.size - 1) HorizontalDivider(color = Slate100)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}