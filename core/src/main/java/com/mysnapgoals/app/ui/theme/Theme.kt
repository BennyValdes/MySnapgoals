package com.mysnapgoals.app.ui.theme

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
    background = ChineseBlack,
    surface = BlueAbyssal,
    onSurface = Beige,
    onBackground = Beige,

    primary = BlueFantastic,
    onPrimary = Beige,
    primaryContainer = DarkJungle,
    onPrimaryContainer = Beige,

    secondary = Charcoal,
    onSecondary = Beige,
    secondaryContainer = DarkJungle,
    onSecondaryContainer = Beige,

    tertiary = BlueAbyssal,
    onTertiary = Beige,
    tertiaryContainer = Charcoal,
    onTertiaryContainer = Beige,

    surfaceVariant = BlueAbyssal,
    onSurfaceVariant = Beige,

    outline = Beige.copy(alpha = 0.8f),
    outlineVariant = Beige.copy(alpha = 0.55f)
)

private val LightColorScheme = lightColorScheme(
    background = NearWhite,
    surface = NearWhite,
    onSurface = Graphite,
    onBackground = Graphite,

    primary = TaupeSoft,
    onPrimary = Graphite,
    primaryContainer = Sand,
    onPrimaryContainer = Graphite,

    secondary = Pebble,
    onSecondary = Graphite,
    secondaryContainer = Sand,
    onSecondaryContainer = Graphite,

    tertiary = Rose,
    onTertiary = Graphite,
    tertiaryContainer = NearWhite,
    onTertiaryContainer = Graphite,

    surfaceVariant = Sand,
    onSurfaceVariant = Graphite,

    outline = Graphite,
    outlineVariant = Graphite.copy(alpha = 0.45f)
)

@Composable
fun SnapGoalsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        typography = Typography,
        content = content
    )
}

