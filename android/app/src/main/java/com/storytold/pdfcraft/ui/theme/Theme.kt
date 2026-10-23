package com.storytold.pdfcraft.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CraftDarkPrimary,
    onPrimary = CraftDarkOnPrimary,
    primaryContainer = CraftDarkPrimaryContainer,
    onPrimaryContainer = CraftDarkOnPrimaryContainer,
    background = CraftDarkBackground,
    onBackground = CraftDarkOnBackground,
    surface = CraftDarkSurface,
    onSurface = CraftDarkOnSurface,
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
    onSurfaceVariant = CraftOnSurfaceVariant,
)

@Composable
fun PdfCraftTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
        content = content
    )
}
