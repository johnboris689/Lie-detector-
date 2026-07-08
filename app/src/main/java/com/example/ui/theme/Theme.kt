package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CyberColorScheme = darkColorScheme(
    primary = CyberPrimary,
    secondary = CyberSecondary,
    tertiary = CyberTertiary,
    background = CyberBackground,
    surface = CyberSurface,
    onPrimary = CyberOnPrimary,
    onBackground = CyberOnBackground,
    onSurface = CyberOnSurface,
    primaryContainer = CyberSecondary,
    onPrimaryContainer = CyberPrimary
)

private val NeonColorScheme = darkColorScheme(
    primary = NeonPrimary,
    secondary = NeonSecondary,
    tertiary = NeonTertiary,
    background = NeonBackground,
    surface = NeonSurface,
    onPrimary = NeonOnPrimary,
    onBackground = NeonOnBackground,
    onSurface = NeonOnSurface,
    primaryContainer = NeonSecondary,
    onPrimaryContainer = NeonPrimary
)

private val FbiColorScheme = darkColorScheme(
    primary = FbiPrimary,
    secondary = FbiSecondary,
    tertiary = FbiTertiary,
    background = FbiBackground,
    surface = FbiSurface,
    onPrimary = FbiOnPrimary,
    onBackground = FbiOnBackground,
    onSurface = FbiOnSurface,
    primaryContainer = FbiSecondary,
    onPrimaryContainer = FbiPrimary
)

@Composable
fun TruthScanTheme(
    themeName: String = "Cyber",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "Neon" -> NeonColorScheme
        "FBI" -> FbiColorScheme
        else -> CyberColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Retain legacy template theme mapping for backward-compatibility with template generated files
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    TruthScanTheme(themeName = "Cyber", content = content)
}
