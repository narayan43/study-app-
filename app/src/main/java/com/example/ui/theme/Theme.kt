package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PoliceGoldLight,
    onPrimary = PoliceNavyDark,
    primaryContainer = PoliceNavyLight,
    onPrimaryContainer = PoliceGoldLight,
    secondary = PoliceGoldSecondary,
    onSecondary = Color.Black,
    secondaryContainer = PoliceNavyPrimary,
    onSecondaryContainer = Color.White,
    tertiary = PoliceRedLight,
    onTertiary = Color.White,
    background = SurfaceDark,
    onBackground = Color(0xFFE2E4E9),
    surface = CardBackgroundDark,
    onSurface = Color(0xFFE2E4E9),
    surfaceVariant = Color(0xFF252C3D),
    onSurfaceVariant = Color(0xFFCACED9)
)

private val LightColorScheme = lightColorScheme(
    primary = PoliceNavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EEF8),
    onPrimaryContainer = PoliceNavyDark,
    secondary = PoliceGoldSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF9F3E5),
    onSecondaryContainer = PoliceNavyDark,
    tertiary = PoliceRedTertiary,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = Color(0xFF191C21),
    surface = CardBackgroundLight,
    onSurface = Color(0xFF191C21),
    surfaceVariant = Color(0xFFEFF2F8),
    onSurfaceVariant = Color(0xFF434752)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent UP Police branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
