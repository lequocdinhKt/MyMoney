package com.example.mymoney.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary          = Blue40,
    onPrimary        = OnPrimaryLight,
    primaryContainer = BlueContainer80,
    onPrimaryContainer = OnPrimaryDark,
    secondary        = GrayMedium,
    onSecondary      = OnPrimaryLight,
    secondaryContainer = GrayLight,
    onSecondaryContainer = OnBackgroundLight,
    tertiary         = WarningAmber,
    onTertiary       = OnWarning,
    tertiaryContainer = WarningContainer,
    onTertiaryContainer = OnWarning,
    error            = ErrorRed,
    onError          = OnError,
    errorContainer   = ErrorContainer,
    onErrorContainer = ErrorRed,
    background       = BackgroundLight,
    onBackground     = OnBackgroundLight,
    surface          = SurfaceLight,
    onSurface        = OnBackgroundLight,
    surfaceVariant   = GrayLight,
    onSurfaceVariant = GrayMedium,
)

private val DarkColorScheme = darkColorScheme(
    primary          = Blue50,
    onPrimary        = OnPrimaryDark,
    primaryContainer = BlueContainer30,
    onPrimaryContainer = Blue50,
    secondary        = GrayMedium,
    onSecondary      = OnBackgroundDark,
    secondaryContainer = SurfaceDark,
    onSecondaryContainer = OnBackgroundDark,
    tertiary         = WarningAmber,
    onTertiary       = OnWarning,
    tertiaryContainer = Color(0xFF3D2800),  // hsl(38, 100%, 12%)
    onTertiaryContainer = WarningAmber,
    error            = ErrorRed,
    onError          = OnError,
    errorContainer   = Color(0xFF4D0000),   // hsl(0, 100%, 15%)
    onErrorContainer = ErrorRed,
    background       = BackgroundDark,
    onBackground     = OnBackgroundDark,
    surface          = SurfaceDark,
    onSurface        = OnBackgroundDark,
    surfaceVariant   = Color(0xFF1E3247),   // hsl(207, 40%, 20%)
    onSurfaceVariant = GrayMedium,
)

@Composable
fun MyMoneyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}