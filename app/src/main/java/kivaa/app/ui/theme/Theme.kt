package kivaa.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun DynamicLogo(modifier: Modifier = Modifier) {
    val config = ThemeManager.currentConfig.value
    val logoUrl = config?.logoUrl

    if (logoUrl != null) {
        Box(modifier = modifier) {
            AsyncImage(
                model = logoUrl,
                contentDescription = "Platform Logo",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheIndustrialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val config = ThemeManager.currentConfig.value

    val colorScheme = if (config?.theme != null) {
        val theme = config.theme!!
        val dynamicPrimary = ThemeManager.getColor(theme.primary, Purple40)
        val dynamicSecondary = ThemeManager.getColor(theme.secondary, PurpleGrey40)
        val dynamicAccent = ThemeManager.getColor(theme.accent, Pink40)
        val dynamicBackground = ThemeManager.getColor(theme.background, Color(0xFFFFFBFE))
        val dynamicText = ThemeManager.getColor(theme.text, Color(0xFF1C1B1F))

        lightColorScheme(
            primary = dynamicPrimary,
            onPrimary = Color.White,
            primaryContainer = dynamicPrimary.copy(alpha = 0.12f),
            onPrimaryContainer = dynamicPrimary,
            
            secondary = dynamicSecondary,
            onSecondary = Color.White,
            secondaryContainer = Color.Transparent, // Removed pill highlight in Nav Bar
            onSecondaryContainer = dynamicPrimary,
            
            tertiary = dynamicAccent,
            onTertiary = Color.White,
            
            background = Color.White, // Hardcoded White
            surface = Color.White,    // Hardcoded White
            onBackground = dynamicText,
            onSurface = dynamicText,
            outlineVariant = dynamicPrimary.copy(alpha = 0.1f)
        )
    } else {
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    }

    // --- Force Dark Icons on White Status Bar ---
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
        }
    }

    // Disable Ripple (glow on press) globally
    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
