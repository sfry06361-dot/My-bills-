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
    primary = IndigoDarkPrimary,
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = IndigoDarkContainer,
    onPrimaryContainer = IndigoDarkOnContainer,
    secondary = SkyDarkSecondary,
    onSecondary = Color(0xFF082F49),
    secondaryContainer = SkyDarkContainer,
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary = AmberLight,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = AmberOnContainer,
    onTertiaryContainer = AmberContainer,
    error = Color(0xFFFB7185),
    onError = Color(0xFF4C0519),
    errorContainer = AlertOverdueOnContainer,
    onErrorContainer = AlertOverdueContainer,
    background = DarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkCardSurface,
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = IndigoContainer,
    onPrimaryContainer = IndigoOnContainer,
    secondary = SkySecondary,
    onSecondary = Color.White,
    secondaryContainer = SkyContainer,
    onSecondaryContainer = SkyOnContainer,
    tertiary = AmberAccent,
    onTertiary = Color.White,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = AmberOnContainer,
    error = AlertOverdue,
    onError = Color.White,
    errorContainer = AlertOverdueContainer,
    onErrorContainer = AlertOverdueOnContainer,
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B)
)

@Composable
fun BillTrackTheme(
    appTheme: String = "System", // System, Light, Dark
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        "Light" -> false
        "Dark" -> true
        else -> isSystemInDarkTheme()
    }
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
