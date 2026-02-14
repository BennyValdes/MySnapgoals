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
import com.mysnapgoals.app.settings.AppTheme

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
    outlineVariant = Beige.copy(alpha = 0.55f),
    error = PinkDust
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
    outlineVariant = Graphite.copy(alpha = 0.45f),
    error = PinkPlum
)

private val PinkLightColorScheme = lightColorScheme(
    background = PinkCream,
    surface = PinkCream,
    onSurface = PinkPlum,
    onBackground = PinkPlum,

    primary = PinkRose,
    onPrimary = PinkCream,
    primaryContainer = PinkDust,
    onPrimaryContainer = PinkPlum,

    secondary = PinkDust,
    onSecondary = PinkPlum,
    secondaryContainer = PinkBlush,
    onSecondaryContainer = PinkPlum,

    tertiary = PinkBlush,
    onTertiary = PinkPlum,
    tertiaryContainer = PinkCream,
    onTertiaryContainer = PinkPlum,

    surfaceVariant = PinkBlush,
    onSurfaceVariant = PinkPlum,

    outline = PinkPlum,
    outlineVariant = PinkPlum.copy(alpha = 0.45f),
    error = PinkPlum
)

private val PinkDarkColorScheme = darkColorScheme(
    background = PinkNight,
    surface = PinkPlum,
    onSurface = PinkCream,
    onBackground = PinkCream,

    primary = PinkRose,
    onPrimary = PinkCream,
    primaryContainer = PinkPlum,
    onPrimaryContainer = PinkCream,

    secondary = PinkDust,
    onSecondary = PinkPlum,
    secondaryContainer = PinkPlum,
    onSecondaryContainer = PinkCream,

    tertiary = PinkBlush,
    onTertiary = PinkPlum,
    tertiaryContainer = PinkPlum,
    onTertiaryContainer = PinkCream,

    surfaceVariant = PinkPlum,
    onSurfaceVariant = PinkCream,

    outline = PinkCream.copy(alpha = 0.8f),
    outlineVariant = PinkCream.copy(alpha = 0.55f),
    error = PinkPlum
)

private val CoffeeLightColorScheme = lightColorScheme(
    background = CoffeeCream,
    surface = CoffeeCream,
    onSurface = CoffeeMocha,
    onBackground = CoffeeMocha,

    primary = CoffeeCaramel,
    onPrimary = CoffeeCream,
    primaryContainer = CoffeeLatte,
    onPrimaryContainer = CoffeeMocha,

    secondary = CoffeeLatte,
    onSecondary = CoffeeMocha,
    secondaryContainer = CoffeeMilk,
    onSecondaryContainer = CoffeeMocha,

    tertiary = CoffeeMilk,
    onTertiary = CoffeeMocha,
    tertiaryContainer = CoffeeCream,
    onTertiaryContainer = CoffeeMocha,

    surfaceVariant = CoffeeMilk,
    onSurfaceVariant = CoffeeMocha,

    outline = CoffeeMocha,
    outlineVariant = CoffeeMocha.copy(alpha = 0.45f),
    error = PinkDust
)

private val CoffeeDarkColorScheme = darkColorScheme(
    background = CoffeeEspresso,
    surface = CoffeeEspresso,
    onSurface = CoffeeCream,
    onBackground = CoffeeCream,

    primary = CoffeeMocha,
    onPrimary = CoffeeCream,
    primaryContainer = CoffeeEspresso,
    onPrimaryContainer = CoffeeCream,

    secondary = CoffeeCaramel,
    onSecondary = CoffeeMocha,
    secondaryContainer = CoffeeEspresso,
    onSecondaryContainer = CoffeeCream,

    tertiary = CoffeeLatte,
    onTertiary = CoffeeMocha,
    tertiaryContainer = CoffeeEspresso,
    onTertiaryContainer = CoffeeCream,

    surfaceVariant = CoffeeEspresso,
    onSurfaceVariant = CoffeeCream,

    outline = CoffeeCream.copy(alpha = 0.7f),
    outlineVariant = CoffeeCream.copy(alpha = 0.45f),
    error = PinkDust
)

@Composable
fun SnapGoalsTheme(
    dynamicColor: Boolean = false,
    themeOption: AppTheme = AppTheme.LIGHT,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeOption) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.PINK -> isSystemInDarkTheme()
        AppTheme.COFFEE -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        themeOption == AppTheme.PINK -> if (darkTheme) PinkDarkColorScheme else PinkLightColorScheme
        themeOption == AppTheme.COFFEE -> if (darkTheme) CoffeeDarkColorScheme else CoffeeLightColorScheme
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

