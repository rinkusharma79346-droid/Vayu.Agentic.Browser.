package com.vayu.agenticbrowser.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val VayuDarkColorScheme = darkColorScheme(
    primary = VayuCyan,
    onPrimary = VayuNavy,
    primaryContainer = VayuCyanDark,
    onPrimaryContainer = VayuCyanLight,
    secondary = VayuTeal,
    onSecondary = VayuNavy,
    secondaryContainer = VayuTealDark,
    onSecondaryContainer = VayuTeal,
    tertiary = VayuViolet,
    onTertiary = Color.White,
    tertiaryContainer = VayuVioletDark,
    onTertiaryContainer = Color(0xFFE8DAFF),
    error = VayuError,
    onError = Color.White,
    errorContainer = Color(0xFF4A1010),
    onErrorContainer = Color(0xFFFFB4AB),
    background = VayuNavy,
    onBackground = VayuOnSurface,
    surface = VayuSurfaceDark,
    onSurface = VayuOnSurface,
    surfaceVariant = VayuSurfaceCard,
    onSurfaceVariant = VayuOnSurfaceVariant,
    outline = VayuOnSurfaceDim,
    outlineVariant = Color(0xFF30363D),
    inverseSurface = VayuOnSurface,
    inverseOnSurface = VayuNavy,
    inversePrimary = VayuCyanDark,
    surfaceTint = VayuCyan,
    scrim = Color.Black
)

private val VayuLightColorScheme = lightColorScheme(
    primary = VayuCyanDark,
    onPrimary = Color.White,
    primaryContainer = VayuCyanLight,
    onPrimaryContainer = VayuNavy,
    secondary = VayuTealDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = VayuNavy,
    tertiary = VayuViolet,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8DAFF),
    onTertiaryContainer = VayuNavy,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
)

@Composable
fun VAYUTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Always use VAYU dark scheme as default — it's the brand identity
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> VayuDarkColorScheme
        else -> VayuLightColorScheme
    }

    // Make status/nav bars transparent for immersive feel
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
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
