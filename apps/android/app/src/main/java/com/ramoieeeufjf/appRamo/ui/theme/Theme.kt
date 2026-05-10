package com.ramoieeeufjf.appRamo.ui.theme

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
    primary = IeeeBlueDark,
    onPrimary = Color(0xFF00344F),
    secondary = BranchGreenDark,
    onSecondary = Color(0xFF003827),
    tertiary = WarmGoldDark,
    onTertiary = Color(0xFF3D2A00),
    background = AppDarkBackground,
    onBackground = Color(0xFFE5E8EB),
    surface = AppDarkSurface,
    onSurface = Color(0xFFE5E8EB),
    surfaceVariant = AppDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC3CBD2),
    outline = Color(0xFF8A949D),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

private val LightColorScheme = lightColorScheme(
    primary = IeeeBlue,
    onPrimary = Color.White,
    secondary = BranchGreen,
    onSecondary = Color.White,
    tertiary = WarmGold,
    onTertiary = Color(0xFF241A00),
    background = AppLightBackground,
    onBackground = Color(0xFF161A1D),
    surface = AppLightSurface,
    onSurface = Color(0xFF161A1D),
    surfaceVariant = AppLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF46545F),
    outline = Color(0xFF6D7882),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

@Composable
fun REIEEEUFJFTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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
