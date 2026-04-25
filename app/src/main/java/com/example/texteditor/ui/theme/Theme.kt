package com.example.texteditor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val LightColors = lightColorScheme(
    primary = InkBlack,
    onPrimary = PaperWhite,
    background = PaperWhite,
    surface = Color.White,
    surfaceVariant = SurfaceGray,
    onBackground = InkBlack,
    onSurface = InkBlack,
    onSurfaceVariant = MutedGray,
    outline = Color(0xFFD4D4D4)
)

private val DarkColors = darkColorScheme(
    primary = PaperWhite,
    onPrimary = InkBlack,
    background = Color(0xFF111111),
    surface = Color(0xFF1C1C1C),
    surfaceVariant = Color(0xFF252525),
    onBackground = Color(0xFFEAEAEA),
    onSurface = Color(0xFFEAEAEA),
    onSurfaceVariant = Color(0xFF888888),
    outline = Color(0xFF333333)
)

@Composable
fun RichTextEditorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}