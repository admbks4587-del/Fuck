package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = StudioObsidian,
    primaryContainer = GoldDark,
    onPrimaryContainer = GoldLight,
    secondary = GoldLight,
    onSecondary = StudioObsidian,
    secondaryContainer = StudioCardBorder,
    onSecondaryContainer = ParchmentWhite,
    tertiary = AmberAccent,
    onTertiary = StudioObsidian,
    background = StudioObsidian,
    onBackground = ParchmentWhite,
    surface = StudioDarkSurface,
    onSurface = ParchmentWhite,
    surfaceVariant = StudioCardBg,
    onSurfaceVariant = ParchmentMuted,
    outline = StudioCardBorder,
    outlineVariant = SlateDivider,
    error = CrimsonAccent,
    onError = Color.White
)

@Composable
fun InfiniteBookTheme(
    darkTheme: Boolean = true, // Default to rich studio dark
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
