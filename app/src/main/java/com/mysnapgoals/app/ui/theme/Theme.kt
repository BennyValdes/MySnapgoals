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
    background = BlueAbyssal,
    surface = BlueAbyssal,
    onSurface = Beige,
    onBackground = Beige,

    primary = BlueFantastic,
    onPrimary = Beige,
    primaryContainer = DarkJungle, // Filter Buttons and Text
    onPrimaryContainer = Beige,

    secondary = BlueFantastic,
    onSecondary = Beige,
    secondaryContainer = DarkJungle,     // Button background
    onSecondaryContainer = Beige,        // IMPORTANTE

    tertiary = GoldShadow,
    onTertiary = Beige,
    tertiaryContainer = GoldShadow,         // IMPORTANTE
    onTertiaryContainer = Beige,         // IMPORTANTE

    surfaceVariant = DarkJungle,         // Plus button
    onSurfaceVariant = Beige,            // IMPORTANTE

    outline = Beige,
    outlineVariant = Beige.copy(alpha = 0.6f)
)

private val LightColorScheme = lightColorScheme(
    background = Beige,
    surface = Beige,
    onSurface = Brown,
    onBackground = Brown,

    primary = Color3,
    onPrimary = Brown,
    primaryContainer = Beige2,
    onPrimaryContainer = Brown,

    secondary = Color3,
    onSecondary = Brown,
    secondaryContainer = Beige2,              // IMPORTANTE
    onSecondaryContainer = Brown,             // IMPORTANTE

    tertiary = Color4,
    onTertiary = Brown,
    tertiaryContainer = Color4,               // IMPORTANTE
    onTertiaryContainer = Brown,              // IMPORTANTE

    surfaceVariant = Beige2,                  // IMPORTANTE (CalendarBanner/PercentageLine)
    onSurfaceVariant = Brown,                 // IMPORTANTE

    outline = Brown,
    outlineVariant = Brown.copy(alpha = 0.5f) // opcional pero recomendado
)

@Composable
fun SnapGoalsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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