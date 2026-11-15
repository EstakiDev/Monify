package dev.estaki.myFinancialApp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dev.estaki.ui_utils.ui.theme.DarkGray
import dev.estaki.ui_utils.ui.theme.TypographyDark
import dev.estaki.ui_utils.ui.theme.TypographyLite
import dev.estaki.ui_utils.ui.theme.White
import dev.estaki.ui_utils.LocalDarkTheme

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFCA73),
    onPrimary = Color(0xFF402B00),
    primaryContainer = Color(0xFF594000),
    onPrimaryContainer = Color(0xFFFFDEAA),
    secondary = Color(0xFFAACAD8),
    onSecondary = Color(0xFF163541),
    secondaryContainer = Color(0xFF2E4B58),
    onSecondaryContainer = Color(0xFFC6E7F5),
    background = Color(0xFF101820),
    onBackground = Color(0xFFE1E5E8),
    surface = Color(0xFF101820),
    onSurface = Color(0xFFE1E5E8),
    surfaceVariant = Color(0xFF40484C),
    onSurfaceVariant = Color(0xFFC0C8CB),
    surfaceContainerLowest = Color(0xFF0B1219),
    surfaceContainerLow = Color(0xFF182128),
    surfaceContainer = Color(0xFF1C252C),
    surfaceContainerHigh = Color(0xFF263038),
    surfaceContainerHighest = Color(0xFF313B43),
    inverseSurface = DarkGray,
    inverseOnSurface = White

)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF805600),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDEAA),
    onPrimaryContainer = Color(0xFF291800),
    secondary = Color(0xFF46636F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC6E7F5),
    onSecondaryContainer = Color(0xFF001F29),
    background = Color(0xFFFAF9F6),
    onBackground = Color(0xFF1A2023),
    surface = Color(0xFFFAF9F6),
    onSurface = Color(0xFF1A2023),
    surfaceVariant = Color(0xFFDEE3E5),
    onSurfaceVariant = Color(0xFF40484C),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF3F4F2),
    surfaceContainer = Color(0xFFEDEEEC),
    surfaceContainerHigh = Color(0xFFE7E9E7),
    surfaceContainerHighest = Color(0xFFE1E4E2),
    inverseSurface = White,
    inverseOnSurface = DarkGray
)

@Composable
fun FinancialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val lightScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicLightColorScheme(context)
        }
        else -> LightColorScheme
    }
    val darkScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(LocalContext.current)
    } else DarkColorScheme
    val progress by animateFloatAsState(if (darkTheme) 1f else 0f, tween(450), label = "theme")
    val colorScheme = lightScheme.copy(
        primary = lerp(lightScheme.primary, darkScheme.primary, progress),
        onPrimary = lerp(lightScheme.onPrimary, darkScheme.onPrimary, progress),
        primaryContainer = lerp(lightScheme.primaryContainer, darkScheme.primaryContainer, progress),
        onPrimaryContainer = lerp(lightScheme.onPrimaryContainer, darkScheme.onPrimaryContainer, progress),
        secondary = lerp(lightScheme.secondary, darkScheme.secondary, progress),
        onSecondary = lerp(lightScheme.onSecondary, darkScheme.onSecondary, progress),
        secondaryContainer = lerp(lightScheme.secondaryContainer, darkScheme.secondaryContainer, progress),
        onSecondaryContainer = lerp(lightScheme.onSecondaryContainer, darkScheme.onSecondaryContainer, progress),
        background = lerp(lightScheme.background, darkScheme.background, progress),
        onBackground = lerp(lightScheme.onBackground, darkScheme.onBackground, progress),
        surface = lerp(lightScheme.surface, darkScheme.surface, progress),
        onSurface = lerp(lightScheme.onSurface, darkScheme.onSurface, progress),
        surfaceVariant = lerp(lightScheme.surfaceVariant, darkScheme.surfaceVariant, progress),
        onSurfaceVariant = lerp(lightScheme.onSurfaceVariant, darkScheme.onSurfaceVariant, progress),
        surfaceContainerLowest = lerp(lightScheme.surfaceContainerLowest, darkScheme.surfaceContainerLowest, progress),
        surfaceContainerLow = lerp(lightScheme.surfaceContainerLow, darkScheme.surfaceContainerLow, progress),
        surfaceContainer = lerp(lightScheme.surfaceContainer, darkScheme.surfaceContainer, progress),
        surfaceContainerHigh = lerp(lightScheme.surfaceContainerHigh, darkScheme.surfaceContainerHigh, progress),
        surfaceContainerHighest = lerp(lightScheme.surfaceContainerHighest, darkScheme.surfaceContainerHighest, progress),
        outline = lerp(lightScheme.outline, darkScheme.outline, progress),
        outlineVariant = lerp(lightScheme.outlineVariant, darkScheme.outlineVariant, progress),
        inverseSurface = lerp(lightScheme.inverseSurface, darkScheme.inverseSurface, progress),
        inverseOnSurface = lerp(lightScheme.inverseOnSurface, darkScheme.inverseOnSurface, progress),
        inversePrimary = lerp(lightScheme.inversePrimary, darkScheme.inversePrimary, progress),
        error = lerp(lightScheme.error, darkScheme.error, progress),
        onError = lerp(lightScheme.onError, darkScheme.onError, progress),
        errorContainer = lerp(lightScheme.errorContainer, darkScheme.errorContainer, progress),
        onErrorContainer = lerp(lightScheme.onErrorContainer, darkScheme.onErrorContainer, progress)
    )
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }


    val typography = TypographyLite.copy(
        bodySmall = TypographyLite.bodySmall.copy(color = lerp(TypographyLite.bodySmall.color, TypographyDark.bodySmall.color, progress)),
        bodyMedium = TypographyLite.bodyMedium.copy(color = lerp(TypographyLite.bodyMedium.color, TypographyDark.bodyMedium.color, progress)),
        bodyLarge = TypographyLite.bodyLarge.copy(color = lerp(TypographyLite.bodyLarge.color, TypographyDark.bodyLarge.color, progress))
    )
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
