package com.example.unipiaudiostories.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.unipiaudiostories.R
import com.example.unipiaudiostories.data.Story
import com.example.unipiaudiostories.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Main Home Screen displaying featured stories, statistics entry, and voice command access.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToStats: () -> Unit,
    onNavigateToStory: (String) -> Unit,
    onNavigateToVoice: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var stories by remember { mutableStateOf<List<Story>>(emptyList()) }

    // Fetch Stories from Firestore
    LaunchedEffect(Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val result = db.collection("stories").get().await()
            stories = result.documents.mapNotNull { it.toObject(Story::class.java)?.copy(id = it.id) }
        } catch (e: Exception) { e.printStackTrace() }
        isLoading = false
    }

    val scrollState = rememberLazyGridState()

    Scaffold(
        topBar = {
            HomeTopBar(onNavigateToVoice)
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(Orange50, Color.White, Color(0xFFFFF0F5))))
        ) {
            LazyVerticalGrid(
                state = scrollState,
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    bottom = 100.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Hero Section ---
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Text(
                            text = stringResource(R.string.app_title),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate800,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.home_subtitle),
                            fontSize = 16.sp,
                            color = Slate500,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Navigate to Statistics
                        StatsBanner(onClick = onNavigateToStats)
                    }
                }

                // --- Section Title ---
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Orange500, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.featured_stories),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                    }
                }

                // --- Story List ---
                if (isLoading) {
                    items(6) { LoadingSkeleton() }
                } else {
                    items(stories) { story ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + scaleIn()
                        ) {
                            ModernStoryCard(story = story, onClick = { onNavigateToStory(story.id) })
                        }
                    }
                }
            }
        }
    }
}

// --- Sub-Components ---

@Composable
fun HomeTopBar(onNavigateToVoice: () -> Unit) {
    Surface(
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth().statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand / Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Orange50,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = Orange500)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.home_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Slate800
                )
            }

            // Actions (Voice & Language)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateToVoice,
                    modifier = Modifier
                        .shadow(4.dp, CircleShape, spotColor = Orange200)
                        .background(
                            Brush.linearGradient(listOf(Orange400, Pink500)),
                            CircleShape
                        )
                        .size(44.dp)
                ) {
                    Icon(Icons.Default.Mic, "Voice", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                LanguageSelector()
            }
        }
    }
}

@Composable
fun StatsBanner(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth().height(100.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(Color(0xFF6B4C9A), Color(0xFF9F69C8))))
        ) {
            // Decorative elements
            Box(modifier = Modifier.offset(x = (-20).dp, y = (-20).dp).size(80.dp).alpha(0.2f).background(Color.White, CircleShape))
            Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 20.dp, y = 20.dp).size(100.dp).alpha(0.2f).background(Color.White, CircleShape))

            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.my_statistics),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = stringResource(R.string.view_progress),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.BarChart, null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernStoryCard(story: Story, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = story.imageUrl,
                contentDescription = story.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 300f
                        )
                    )
            )

            // Play Icon Overlay
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayCircle,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(48.dp).shadow(4.dp, CircleShape)
                )
            }

            // Duration Badge
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${story.durationMinutes} ${stringResource(R.string.min_suffix)}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }

            // Title & Author
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = story.ageRange,
                    color = Orange200,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = story.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                Text(
                    text = story.author,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}