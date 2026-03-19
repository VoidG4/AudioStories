package com.example.unipistories.ui.theme // <--- ΠΡΟΣΟΧΗ: Βάλε το δικό σου package name

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.unipiaudiostories.ui.theme.*

// Ορίζουμε το Light Theme με βάση τα χρώματα του React project (Tailwind)
private val LightColorScheme = lightColorScheme(
    primary = Orange500,        // Το βασικό χρώμα (Κουμπιά, εικονίδια)
    secondary = Pink500,        // Το δευτερεύον (Gradients)
    tertiary = Orange400,
    background = Slate50,       // Το φόντο της εφαρμογής
    surface = Color.White,      // Το φόντο των καρτών (Cards)
    onPrimary = Color.White,    // Χρώμα κειμένου πάνω στο πορτοκαλί
    onSecondary = Color.White,
    onBackground = Slate900,    // Χρώμα κειμένου στο φόντο
    onSurface = Slate900,       // Χρώμα κειμένου στις κάρτες
)

// Ορίζουμε ένα Dark Theme (αν το χρειαστείς μελλοντικά)
private val DarkColorScheme = darkColorScheme(
    primary = Orange400,
    secondary = Pink500,
    background = Slate900,      // Σκούρο φόντο
    surface = Slate800,         // Σκούρες κάρτες
    onPrimary = Slate900,
    onBackground = Slate50,
    onSurface = Slate50
)

@Composable
fun UnipiStoriesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Βάζουμε dynamicColor = false για να ΜΗΝ παίρνει τα χρώματα του κινητού (Material You),
    // αλλά να κρατάει τα δικά μας (Orange/Pink).
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Βάφουμε την μπάρα πάνω (Status Bar)
            window.statusBarColor = colorScheme.background.toArgb()
            // Αν είναι light theme, τα εικονίδια της status bar γίνονται σκούρα
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        // typography = Typography, // Αν σου βγάζει κόκκινο και το Typography, σβήσε αυτή τη γραμμή ή φτιάξε το Type.kt
        content = content
    )
}