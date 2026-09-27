package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TurboDarkColorScheme = darkColorScheme(
    primary = TurboOrange,
    onPrimary = Color.White,
    primaryContainer = TurboOrangeSecondary,
    onPrimaryContainer = Color.White,
    secondary = TurboOrangeSecondary,
    onSecondary = Color.White,
    tertiary = TurboGreen,
    onTertiary = Color.White,
    background = TurboDarkBg,
    onBackground = TurboWhite,
    surface = TurboDarkCard,
    onSurface = TurboWhite,
    surfaceVariant = TurboDarkSurface,
    onSurfaceVariant = TurboGray,
    error = TurboRed,
    onError = Color.White,
    outline = TurboGrayDark
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TurboDarkColorScheme,
        typography = Typography,
        content = content
    )
}
