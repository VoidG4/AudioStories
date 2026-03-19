package com.example.unipiaudiostories

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.unipiaudiostories.ui.home.HomeScreen
import com.example.unipiaudiostories.ui.home.VoiceCommandScreen
import com.example.unipiaudiostories.ui.statistics.StatisticsScreen
import com.example.unipiaudiostories.ui.story.StoryPlayerScreen
import com.example.unipiaudiostories.ui.theme.Orange500
import com.example.unipiaudiostories.ui.theme.Slate50

/**
 * Main entry point of the application.
 * Sets up the Navigation Graph and Theme.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure edge-to-edge display with transparent system bars
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.WHITE, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.WHITE, android.graphics.Color.TRANSPARENT)
        )

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(primary = Orange500, background = Slate50, surface = Color.White)
            ) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {

                    // 1. Home Screen
                    composable("home") {
                        HomeScreen(
                            onNavigateToStats = { navController.navigate("statistics") },
                            onNavigateToStory = { storyId -> navController.navigate("story/$storyId") },
                            onNavigateToVoice = { navController.navigate("voice_command") }
                        )
                    }

                    // 2. Voice Command Screen
                    composable("voice_command") {
                        VoiceCommandScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToStats = {
                                navController.popBackStack()
                                navController.navigate("statistics")
                            },
                            onNavigateToStory = { storyId ->
                                navController.popBackStack()
                                navController.navigate("story/$storyId")
                            }
                        )
                    }

                    // 3. Statistics Screen
                    composable("statistics") {
                        StatisticsScreen(onNavigateBack = { navController.popBackStack() })
                    }

                    // 4. Story Player Screen
                    composable(
                        route = "story/{storyId}",
                        arguments = listOf(navArgument("storyId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("storyId")
                        StoryPlayerScreen(storyId = id, onNavigateBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}