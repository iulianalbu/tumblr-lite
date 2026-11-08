package com.example.tumblrlite.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB5C7FF),
    secondary = Color(0xFFD0BCFF),
    tertiary = Color(0xFF8FD8C5),
    background = Color(0xFF101116),
    surface = Color(0xFF101116),
)

@Composable
fun TumblrLiteTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
