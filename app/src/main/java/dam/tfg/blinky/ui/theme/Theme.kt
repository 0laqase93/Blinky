package dam.tfg.blinky.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import dam.tfg.blinky.utils.ThemeManager

private val DarkColorScheme = darkColorScheme(
    primary = GoogleBlueLight,
    secondary = GoogleBlue,
    tertiary = LightOrange,

    // Background and surface colors
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,

    // On colors (text and icons)
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurface.copy(alpha = 0.8f),

    // Other colors
    error = Color(0xFFCF6679),
    outline = Color(0xFF9AA0A6)
)

private val LightColorScheme = lightColorScheme(
    primary = GoogleBlue,
    secondary = GoogleBlueDark,
    tertiary = LightOrange,

    // Background and surface colors
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,

    // On colors (text and icons)
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurface.copy(alpha = 0.8f),

    // Other colors
    error = Color(0xFFB00020),
    outline = Color(0xFF6F7275)
)

@Composable
fun BlinkyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeManager = ThemeManager.getInstance(context)

    // Get theme preferences from ThemeManager
    val followSystem by themeManager.followSystem
    val isDarkMode by themeManager.isDarkMode

    // Determine if dark theme should be used
    val useDarkTheme = if (followSystem) {
        // Use system setting
        isSystemInDarkTheme()
    } else {
        // Use user preference
        isDarkMode
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
