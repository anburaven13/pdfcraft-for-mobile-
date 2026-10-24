package com.storytold.pdfcraft.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CraftDarkPrimary,
    onPrimary = CraftDarkOnPrimary,
    primaryContainer = CraftDarkPrimaryContainer,
    onPrimaryContainer = CraftDarkOnPrimaryContainer,
    secondary = AcrobatActionBlueLight,
    onSecondary = CraftOnSecondary,
    secondaryContainer = CraftSecondaryContainer,
    onSecondaryContainer = CraftOnSecondaryContainer,
    background = CraftDarkBackground,
    onBackground = CraftDarkOnBackground,
    surface = CraftDarkSurface,
    onSurface = CraftDarkOnSurface,
    surfaceVariant = CraftDarkSurfaceVariant,
    onSurfaceVariant = CraftDarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = CraftPrimary,
    onPrimary = CraftOnPrimary,
    primaryContainer = CraftPrimaryContainer,
    onPrimaryContainer = CraftOnPrimaryContainer,
    secondary = CraftSecondary,
    onSecondary = CraftOnSecondary,
    secondaryContainer = CraftSecondaryContainer,
    onSecondaryContainer = CraftOnSecondaryContainer,
    background = CraftBackground,
    onBackground = CraftOnBackground,
    surface = CraftSurface,
    onSurface = CraftOnSurface,
    surfaceVariant = CraftSurfaceVariant,
    onSurfaceVariant = CraftOnSurfaceVariant
)

@Composable
fun PdfCraftTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
