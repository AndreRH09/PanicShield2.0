package com.example.panicshield.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Colores para el tema de la aplicación de pánico
private val md_theme_light_primary = Color(0xFFD32F2F)
private val md_theme_light_onPrimary = Color(0xFFFFFFFF)
private val md_theme_light_primaryContainer = Color(0xFFFFDAD6)
private val md_theme_light_onPrimaryContainer = Color(0xFF410002)
private val md_theme_light_secondary = Color(0xFF775652)
private val md_theme_light_onSecondary = Color(0xFFFFFFFF)
private val md_theme_light_secondaryContainer = Color(0xFFFFDAD6)
private val md_theme_light_onSecondaryContainer = Color(0xFF2C1512)
private val md_theme_light_tertiary = Color(0xFF755A2F)
private val md_theme_light_onTertiary = Color(0xFFFFFFFF)
private val md_theme_light_tertiaryContainer = Color(0xFFFFDDB3)
private val md_theme_light_onTertiaryContainer = Color(0xFF271900)
private val md_theme_light_error = Color(0xFFBA1A1A)
private val md_theme_light_errorContainer = Color(0xFFFFDAD6)
private val md_theme_light_onError = Color(0xFFFFFFFF)
private val md_theme_light_onErrorContainer = Color(0xFF410002)
private val md_theme_light_background = Color(0xFFFFFBFF)
private val md_theme_light_onBackground = Color(0xFF201A19)
private val md_theme_light_surface = Color(0xFFFFFBFF)
private val md_theme_light_onSurface = Color(0xFF201A19)
private val md_theme_light_surfaceVariant = Color(0xFFF5DDDA)
private val md_theme_light_onSurfaceVariant = Color(0xFF534341)
private val md_theme_light_outline = Color(0xFF857371)
private val md_theme_light_inverseOnSurface = Color(0xFFFBEEEC)
private val md_theme_light_inverseSurface = Color(0xFF362F2E)
private val md_theme_light_inversePrimary = Color(0xFFFFB4AB)

private val md_theme_dark_primary = Color(0xFFFFB4AB)
private val md_theme_dark_onPrimary = Color(0xFF690005)
private val md_theme_dark_primaryContainer = Color(0xFF93000A)
private val md_theme_dark_onPrimaryContainer = Color(0xFFFFDAD6)
private val md_theme_dark_secondary = Color(0xFFE7BDB7)
private val md_theme_dark_onSecondary = Color(0xFF442926)
private val md_theme_dark_secondaryContainer = Color(0xFF5D3F3B)
private val md_theme_dark_onSecondaryContainer = Color(0xFFFFDAD6)
private val md_theme_dark_tertiary = Color(0xFFE0C2A6)
private val md_theme_dark_onTertiary = Color(0xFF3F2E04)
private val md_theme_dark_tertiaryContainer = Color(0xFF574419)
private val md_theme_dark_onTertiaryContainer = Color(0xFFFFDDB3)
private val md_theme_dark_error = Color(0xFFFFB4AB)
private val md_theme_dark_errorContainer = Color(0xFF93000A)
private val md_theme_dark_onError = Color(0xFF690005)
private val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
private val md_theme_dark_background = Color(0xFF201A19)
private val md_theme_dark_onBackground = Color(0xFFEDE0DE)
private val md_theme_dark_surface = Color(0xFF201A19)
private val md_theme_dark_onSurface = Color(0xFFEDE0DE)
private val md_theme_dark_surfaceVariant = Color(0xFF534341)
private val md_theme_dark_onSurfaceVariant = Color(0xFFD8C2BF)
private val md_theme_dark_outline = Color(0xFFA08C89)
private val md_theme_dark_inverseOnSurface = Color(0xFF201A19)
private val md_theme_dark_inverseSurface = Color(0xFFEDE0DE)
private val md_theme_dark_inversePrimary = Color(0xFFD32F2F)

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
)

private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
)
@Composable
fun PanicShieldTheme(
      darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}