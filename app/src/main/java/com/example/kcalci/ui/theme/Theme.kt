package com.example.kcalci.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF8B7355),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE8D6),
    onPrimaryContainer = Color(0xFF2C1600),
    secondary = Color(0xFFA0826D),
    onSecondary = Color.White,
    background = Color(0xFFFFF8F0),
    onBackground = Color(0xFF5D4E37),
    surface = Color.White,
    onSurface = Color(0xFF5D4E37),
)

@Composable
fun KcalciTheme(
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}