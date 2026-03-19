package com.example.unipiaudiostories.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import coil.compose.AsyncImage
import com.example.unipiaudiostories.data.Story
import com.example.unipiaudiostories.ui.theme.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.res.stringResource
import com.example.unipiaudiostories.R

/**
 * Dropdown component to toggle application language (English, Greek, German).
 * Uses AppCompatDelegate to persist locale changes.
 */
@Composable
fun LanguageSelector() {
    // Retrieve current application locale
    val currentLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    val currentLang = if (currentLocale.contains("el")) "el" else if (currentLocale.contains("de")) "de" else "en"

    var expanded by remember { mutableStateOf(false) }

    // UI Mappings for flags and labels
    val flags = mapOf("en" to "🇬🇧", "el" to "🇬🇷", "de" to "🇩🇪")
    val labels = mapOf("en" to "English", "el" to "Ελληνικά", "de" to "Deutsch")

    Box {
        TextButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.textButtonColors(contentColor = Slate800)
        ) {
            Text(text = flags[currentLang] ?: "🇬🇧", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            flags.keys.forEach { code ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = flags[code]!!, fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                            Text(text = labels[code]!!, color = Slate900)
                        }
                    },
                    onClick = {
                        // Apply new locale to the application context
                        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(code)
                        AppCompatDelegate.setApplicationLocales(appLocale)
                        expanded = false
                    },
                    modifier = if (currentLang == code) Modifier.background(Orange50) else Modifier
                )
            }
        }
    }
}

/**
 * Legacy Story Card Component.
 * Displays story image, age range, title, author, and duration.
 */
@Composable
fun StoryCard(
    story: Story,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Image Container
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f)) {
                AsyncImage(
                    model = story.imageUrl,
                    contentDescription = story.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 300f
                            )
                        )
                )
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                ) {
                    Text(
                        text = story.ageRange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = story.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 2,
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                )
            }
            // Content Details
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Slate500, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(story.author, fontSize = 12.sp, color = Slate500, maxLines = 1)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = Slate500, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${story.durationMinutes} ${stringResource(R.string.min_suffix)}", fontSize = 12.sp, color = Slate500)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Orange500, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.listen_now), color = Orange500, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }
        }
    }
}

/**
 * Placeholder UI component displayed while data is loading.
 */
@Composable
fun LoadingSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth().height(280.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Slate100))
            Column(modifier = Modifier.padding(16.dp)) {
                Box(modifier = Modifier.height(20.dp).fillMaxWidth(0.7f).background(Slate100, RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.height(16.dp).fillMaxWidth(0.4f).background(Slate100, RoundedCornerShape(4.dp)))
            }
        }
    }
}