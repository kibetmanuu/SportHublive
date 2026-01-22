package ke.nucho.sportshublive.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark Theme Colors (Deep Blue Theme)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1565C0),           // Deep Blue
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0D47A1),  // Darker Blue
    onPrimaryContainer = Color.White,

    secondary = Color(0xFF42A5F5),         // Light Blue
    onSecondary = Color.White,

    background = Color(0xFF121212),        // Very Dark Gray
    onBackground = Color.White,

    surface = Color(0xFF1E1E1E),          // Dark Gray
    onSurface = Color.White,

    surfaceVariant = Color(0xFF2C2C2C),   // Slightly lighter gray
    onSurfaceVariant = Color(0xFFBDBDBD), // Light gray text

    error = Color(0xFFD32F2F),            // Red for errors/live
    onError = Color.White,

    outline = Color(0xFF424242)
)

// Light Theme Colors (optional, can keep dark theme always)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),

    secondary = Color(0xFF42A5F5),
    onSecondary = Color.White,

    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1B1F),

    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),

    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF424242),

    error = Color(0xFFD32F2F),
    onError = Color.White
)

@Composable
fun SportsHubLiveTheme(
    darkTheme: Boolean = true, // Force dark theme by default
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            window.navigationBarColor = Color(0xFF0D47A1).toArgb() // Match bottom nav
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}