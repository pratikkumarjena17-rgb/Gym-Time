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
    primary = GymOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A1E00),
    onPrimaryContainer = GymOrangeLight,
    secondary = GymCyanSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003845),
    onSecondaryContainer = Color(0xFF70E1F5),
    tertiary = GymGreenTertiary,
    onTertiary = Color.Black,
    background = GymDarkBackground,
    onBackground = Color(0xFFF3F4F6),
    surface = GymDarkSurface,
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = GymDarkSurfaceElevated,
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = GymDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = GymOrangeDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEDD5),
    onPrimaryContainer = Color(0xFF7C2D12),
    secondary = GymCyanSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF155E75),
    tertiary = GymGreenTertiary,
    onTertiary = Color.White,
    background = GymLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = GymLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = GymLightSurfaceElevated,
    onSurfaceVariant = Color(0xFF64748B),
    outline = GymLightOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent athletic brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
