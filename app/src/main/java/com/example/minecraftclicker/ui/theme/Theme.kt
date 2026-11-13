package com.example.minecraftclicker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = DarkGreen,
    onPrimary = Cream,
    primaryContainer = Green,
    onPrimaryContainer = Charcoal,
    secondary = Brown,
    onSecondary = Cream,
    secondaryContainer = Caramel,
    onSecondaryContainer = Charcoal,
    tertiary = Caramel,
    onTertiary = Charcoal,
    tertiaryContainer = Sand,
    onTertiaryContainer = Charcoal,
    background = Cream,
    onBackground = Charcoal,
    surface = Cream,
    onSurface = Charcoal,
    surfaceVariant = Sand,
    onSurfaceVariant = Brown,
    surfaceContainerLowest = Cream,
    surfaceContainerLow = Cream,
    surfaceContainer = Sand,
    surfaceContainerHigh = Sand,
    surfaceContainerHighest = Sand,
    outline = Brown,
    outlineVariant = Sand,
    error = ErrorRed,
    onError = Cream
)

private val DarkColors = darkColorScheme(
    primary = Green,
    onPrimary = Charcoal,
    primaryContainer = DarkGreen,
    onPrimaryContainer = Cream,
    secondary = Caramel,
    onSecondary = Charcoal,
    secondaryContainer = Brown,
    onSecondaryContainer = Cream,
    tertiary = Sand,
    onTertiary = Charcoal,
    tertiaryContainer = Brown,
    onTertiaryContainer = Cream,
    background = Charcoal,
    onBackground = Cream,
    surface = Charcoal,
    onSurface = Cream,
    surfaceVariant = LightCharcoal,
    onSurfaceVariant = Sand,
    surfaceContainerLowest = Charcoal,
    surfaceContainerLow = Charcoal,
    surfaceContainer = LightCharcoal,
    surfaceContainerHigh = LightCharcoal,
    surfaceContainerHighest = LightCharcoal,
    outline = Caramel,
    outlineVariant = Brown,
    error = LightErrorRed,
    onError = Charcoal
)

@Composable
fun MinecraftClickerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
