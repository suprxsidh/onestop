package com.suprxsidh.onestop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFB0591C),
    background = Color(0xFFEEF0F4),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1920),
    onSurface = Color(0xFF1A1920)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE2853B),
    background = Color(0xFF17161B),
    surface = Color(0xFF201F26),
    onBackground = Color(0xFFF1EFEA),
    onSurface = Color(0xFFF1EFEA)
)

@Composable
fun OneStopTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
