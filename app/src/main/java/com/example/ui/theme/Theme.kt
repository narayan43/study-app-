package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIsDarkTheme = compositionLocalOf { false }

private val LightColorScheme = lightColorScheme(
    primary = AppPrimary,
    onPrimary = AppOnPrimary,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = AppPrimary,
    secondary = AppPrimary,
    onSecondary = AppOnPrimary,
    background = AppBackground,
    onBackground = AppTextPrimary,
    surface = AppSurface,
    onSurface = AppTextPrimary,
    surfaceVariant = Color(0xFFF1F3F9),
    onSurfaceVariant = AppTextSecondary,
    outline = AppDivider,
    outlineVariant = AppDivider
)

private val DarkColorScheme = darkColorScheme(
    primary = AppPrimaryDark,
    onPrimary = AppOnPrimaryDark,
    primaryContainer = Color(0xFF282D50),
    onPrimaryContainer = AppPrimaryDark,
    secondary = AppPrimaryDark,
    onSecondary = AppOnPrimaryDark,
    background = AppBackgroundDark,
    onBackground = AppTextPrimaryDark,
    surface = AppSurfaceDark,
    onSurface = AppTextPrimaryDark,
    surfaceVariant = Color(0xFF232742),
    onSurfaceVariant = AppTextSecondaryDark,
    outline = AppDividerDark,
    outlineVariant = AppDividerDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default theme = LIGHT (in-app toggle, not system-forced)
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
