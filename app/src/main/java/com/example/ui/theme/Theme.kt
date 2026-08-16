package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SportsAmberLight,
    onPrimary = SportsNavyDark,
    primaryContainer = SportsNavyMedium,
    onPrimaryContainer = SportsAmberLight,
    secondary = SportsCyanAccent,
    onSecondary = SportsNavyDark,
    tertiary = SportsEmeraldSuccess,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = SportsCrimsonDanger
)

private val LightColorScheme = lightColorScheme(
    primary = SportsNavyMedium,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = SportsNavyDark,
    secondary = SportsAmberPrimary,
    onSecondary = Color.Black,
    tertiary = SportsCyanAccent,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = SportsCrimsonDanger
)

@Composable
fun SportsOpsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
