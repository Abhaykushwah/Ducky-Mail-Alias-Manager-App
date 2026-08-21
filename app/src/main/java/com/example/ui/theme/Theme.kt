package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SimpleDarkColorScheme = darkColorScheme(
    primary = SimpleDarkPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = SimpleDarkSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = ProtonPurpleAccent,
    onTertiary = Color.White,
    background = SimpleDarkBackground,
    onBackground = SimpleDarkTextPrimary,
    surface = SimpleDarkSurface,
    onSurface = SimpleDarkTextPrimary,
    surfaceVariant = SimpleDarkSurfaceVariant,
    onSurfaceVariant = SimpleDarkTextSecondary,
    outline = SimpleDarkBorder,
    error = StatusDeactiveRed
)

private val SimpleLightColorScheme = lightColorScheme(
    primary = SimpleLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = SimpleLightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF155E75),
    tertiary = ProtonPurpleAccent,
    onTertiary = Color.White,
    background = SimpleLightBackground,
    onBackground = SimpleLightTextPrimary,
    surface = SimpleLightSurface,
    onSurface = SimpleLightTextPrimary,
    surfaceVariant = SimpleLightSurfaceVariant,
    onSurfaceVariant = SimpleLightTextSecondary,
    outline = SimpleLightBorder,
    error = StatusDeactiveRed
)

@Composable
fun DuckAliasTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SimpleDarkColorScheme else SimpleLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
